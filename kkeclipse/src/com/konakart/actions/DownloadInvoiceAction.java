//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is free software; you can redistribute 
// it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package com.konakart.actions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;
import com.konakart.app.KKException;
import com.konakart.app.PdfOptions;
import com.konakart.app.PdfResult;
import com.konakart.appif.OrderIf;
import com.konakart.util.FileUtils;
import com.konakart.util.KKConstants;

/**
 * Action called just before downloading the invoice
 */
public class DownloadInvoiceAction extends BaseAction
{

    private static final long serialVersionUID = 1L;

    protected static final int DEFAULT_BUFFER_SIZE = 4096;

    private String kkContentType;

    private InputStream kkInputName;

    private String kkContentDisposition;

    private boolean extCall = false;

    /**
     * Typically called from a KonaKart tile not part of the standard storefront application
     * 
     * @return Returns a forward string
     */
    public String externalCall()
    {
        this.extCall = true;
        execute();
        return null;
    }

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        boolean downloaded = false;

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            if (this.extCall == true)
            {
                String sessionId = request.getParameter("sessionId");
                if (sessionId == null)
                {
                    return null;
                }

                // Use the session of the logged in user to initialise kkAppEng
                try
                {
                    kkAppEng.getEng().checkSession(sessionId);
                } catch (KKException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("DownloadInvoiceAction called with invalid session Id :"
                                + sessionId);
                    }
                    return null;
                }

                kkAppEng.getCustomerMgr().loginBySession(sessionId);
            }

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "MyAccount");
            if (custId < 0)
            {
                return KKLOGIN;
            }

            String orderIdStr = request.getParameter("orderId");

            if (orderIdStr == null)
            {
                return "MyAccount";
            }

            int orderId;
            try
            {
                orderId = Integer.parseInt(orderIdStr);
            } catch (RuntimeException e)
            {
                return "MyAccount";
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getEng().getOrder(kkAppEng.getSessionId(), orderId,
                    kkAppEng.getLangId());

            if (order == null)
            {
                return "MyAccount";
            }

            // Determine whether a pdf document exists on the file system. Otherwise we create it on
            // the fly.
            if (order.getInvoiceFilename() != null && order.getInvoiceFilename().length() > 0)
            {
                // Try to open the file
                String pdfBase = kkAppEng.getConfig(KKConstants.CONF_KEY_PDF_BASE_DIRECTORY);
                if (pdfBase == null)
                {
                    pdfBase = "";
                }
                String fullFileName = pdfBase + FileUtils.FILE_SEPARATOR
                        + KKAppEng.getEngConf().getStoreId() + FileUtils.FILE_SEPARATOR
                        + order.getInvoiceFilename();

                File file = new File(fullFileName);
                if (file.canRead() == false)
                {
                    throw new Exception("The file " + fullFileName + " cannot be opened");
                }

                // set the content type
                response.setContentType("application/pdf");

                // Comment out the following line if you want the invoice to open up in the browser
                // window
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + order.getInvoiceFilename() + "\"");

                // Define input and output streams
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ServletOutputStream os = response.getOutputStream();

                /*
                 * Set the downloaded flag as soon as we get the output stream from the response.
                 * Once we have got the output stream we must return null from this method otherwise
                 * we get an exception if we try to do a mapping.findForward() because it also calls
                 * response.getOutputStream
                 */
                downloaded = true;

                // Copy from input to output
                try
                {
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int n = 0;

                    /*
                     * In the case of IE it stops here until the user decides to download or cancel
                     * out. Therefore if the operation is cancelled we do not update the download
                     * count. In the case of Firefox, this method is run completely before the
                     * download dialogue box is displayed. This means that the download count is
                     * updated even if the user decides to cancel the operation.
                     */
                    while ((n = bis.read(buffer)) > -1)
                    {
                        os.write(buffer, 0, n);
                    }
                } finally
                {
                    bis.close();
                    if (os != null)
                    {
                        os.flush();
                        os.close();
                    }
                }
            } else
            {
                // Create the invoice
                PdfResult pdfResult = createInvoice(kkAppEng, order);

                if (pdfResult == null || pdfResult.getPdfBytes() == null)
                {
                    throw new KKAppException("Unable to create the PDF invoice");
                }

                // set the content type
                response.setContentType("application/pdf");

                // Comment out the following line if you want the invoice to open up in the browser
                // window
                response.setHeader("Content-Disposition", "attachment; filename=\"" + "order_"
                        + order.getId() + ".pdf" + "\"");

                ServletOutputStream os = null;
                try
                {
                    os = response.getOutputStream();

                    /*
                     * Set the downloaded flag as soon as we get the output stream from the
                     * response. Once we have got the output stream we must return null from this
                     * method otherwise we get an exception if we try to do a mapping.findForward()
                     * because it also calls response.getOutputStream
                     */
                    downloaded = true;

                    // Write the pdf to the output stream
                    os.write(pdfResult.getPdfBytes());

                } finally
                {
                    if (os != null)
                    {
                        os.flush();
                        os.close();
                    }
                }
            }
            if (downloaded)
            {
                return null;
            }

            return "MyAccount";

        } catch (Exception e)
        {
            if (downloaded)
            {
                return null;
            }
            return super.handleException(request, e);
        }
    }

    /**
     * Used to download when running as a portlet
     * 
     * @return Returns a String
     */
    public String executePortlet()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            String orderIdStr = request.getParameter("orderId");

            if (orderIdStr == null)
            {
                return "MyAccount";
            }

            int orderId;
            try
            {
                orderId = Integer.parseInt(orderIdStr);
            } catch (RuntimeException e)
            {
                return "MyAccount";
            }

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "MyAccount");
            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Get the order
            OrderIf order = kkAppEng.getEng().getOrder(kkAppEng.getSessionId(), orderId,
                    kkAppEng.getLangId());

            // Determine whether a pdf document exists on the file system. Otherwise we create it on
            // the fly.
            if (order.getInvoiceFilename() != null && order.getInvoiceFilename().length() > 0)
            {
                // Try to open the file
                String pdfBase = kkAppEng.getConfig(KKConstants.CONF_KEY_PDF_BASE_DIRECTORY);
                if (pdfBase == null)
                {
                    pdfBase = "";
                }
                String fullFileName = pdfBase + FileUtils.FILE_SEPARATOR
                        + KKAppEng.getEngConf().getStoreId() + FileUtils.FILE_SEPARATOR
                        + order.getInvoiceFilename();

                File file = new File(fullFileName);
                if (file.canRead() == false)
                {
                    throw new Exception("The file " + fullFileName + " cannot be opened");
                }

                kkContentDisposition = "attachment;filename=\"" + file.getName() + "\"";
                kkContentType = "application/pdf";
                kkInputName = new FileInputStream(file);

            } else
            {
                // Create the invoice
                PdfResult pdfResult = createInvoice(kkAppEng, order);

                if (pdfResult == null || pdfResult.getPdfBytes() == null)
                {
                    throw new KKAppException("Unable to create the PDF invoice");
                }

                kkContentDisposition = "attachment;filename=\"" + "order_" + order.getId() + ".pdf"
                        + "\"";
                kkContentType = "application/pdf";
                kkInputName = new ByteArrayInputStream(pdfResult.getPdfBytes());
            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * Creates the PDF invoice using the AdminPdfMgr if the code is present. This is normally
     * available only for the enterprise version of KonaKart. If the invoice already exists in the
     * file system then this copy of the invoice is used, otherwise it is created temporarily.
     * 
     * @param order
     * @throws Exception
     */
    private PdfResult createInvoice(KKAppEng kkAppEng, OrderIf order) throws Exception
    {
        int langId = kkAppEng.getLangId();

        PdfOptions options = new PdfOptions();
        options.setId(order.getId());
        options.setType(KKConstants.HTML_ORDER_INVOICE);
        options.setLanguageId(langId);
        options.setReturnFileName(false);
        options.setReturnBytes(true);
        options.setCreateFile(false);

        PdfResult pdfResult = (PdfResult) kkAppEng.getEng()
                .getPdf(kkAppEng.getSessionId(), options);

        return pdfResult;
    }

    /**
     * @return the kkContentType
     */
    public String getKkContentType()
    {
        return kkContentType;
    }

    /**
     * @param kkContentType
     *            the kkContentType to set
     */
    public void setKkContentType(String kkContentType)
    {
        this.kkContentType = kkContentType;
    }

    /**
     * @return the kkContentDisposition
     */
    public String getKkContentDisposition()
    {
        return kkContentDisposition;
    }

    /**
     * @param kkContentDisposition
     *            the kkContentDisposition to set
     */
    public void setKkContentDisposition(String kkContentDisposition)
    {
        this.kkContentDisposition = kkContentDisposition;
    }

    /**
     * @return the kkInputName
     */
    public InputStream getKkInputName()
    {
        return kkInputName;
    }

    /**
     * @param kkInputName
     *            the kkInputName to set
     */
    public void setKkInputName(InputStream kkInputName)
    {
        this.kkInputName = kkInputName;
    }

}
