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
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;
import com.konakart.appif.DigitalDownloadIf;
import com.konakart.util.FileUtils;
import com.konakart.util.Utils;

/**
 * Action called just before doing the digital download
 */
public class DigitalDownloadAction extends BaseAction
{

    private static final long serialVersionUID = 1L;

    protected static final int DEFAULT_BUFFER_SIZE = 4096;

    private String kkContentType;

    private FileInputStream kkInputName;

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

            String ddIdStr = request.getParameter("ddId");

            if (ddIdStr == null)
            {
                return "MyAccount";
            }

            int ddId;
            try
            {
                ddId = Integer.parseInt(ddIdStr);
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

            // Get the Digital Downloads
            DigitalDownloadIf[] downloads = kkAppEng.getProductMgr().getDigitalDownloads();

            if (downloads == null || downloads.length == 0)
            {
                return "MyAccount";
            }

            for (int i = 0; i < downloads.length; i++)
            {
                DigitalDownloadIf dd = downloads[i];
                if (dd != null && (dd.getId() == ddId) && dd.getProduct() != null)
                {
                    // Get the file
                    String fullPath = kkAppEng.getDdbasePath();
                    if (dd.getFilePath() != null)
                    {
                        fullPath = dd.getFilePath();
                    } else
                    {
                        fullPath = fullPath + FileUtils.FILE_SEPARATOR
                                + dd.getProduct().getFilePath();
                    }
                    fullPath = Utils.removeMultipleSlashes(fullPath);

                    File file = new File(fullPath);
                    if (file.canRead() == false)
                    {
                        throw new Exception("The file " + fullPath + " cannot be opened");
                    }

                    // Set the content type
                    String contentType = dd.getProduct().getContentType();
                    if (contentType != null && contentType.length() > 0)
                    {
                        response.setContentType(contentType);
                    }

                    // Download as an attachment if configured to do so
                    if (kkAppEng.isDdAsAttachment())
                    {
                        response.setHeader("Content-Disposition",
                                "attachment; filename=\"" + file.getName() + "\"");
                    }

                    // Define input and output streams
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ServletOutputStream os = response.getOutputStream();

                    /*
                     * Set the downloaded flag as soon as we get the output stream from the
                     * response. Once we have got the output stream we must return null from this
                     * method otherwise we get an exception if we try to do a mapping.findForward()
                     * because it also calls response.getOutputStream
                     */
                    downloaded = true;

                    // Copy from input to output
                    try
                    {
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int n = 0;

                        /*
                         * In the case of IE it stops here until the user decides to download or
                         * cancel out. Therefore if the operation is cancelled we do not update the
                         * download count. In the case of Firefox, this method is run completely
                         * before the download dialogue box is displayed. This means that the
                         * download count is updated even if the user decides to cancel the
                         * operation.
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

                    // Update the downloaded count
                    kkAppEng.getEng().updateDigitalDownloadCountById(kkAppEng.getSessionId(),
                            dd.getId());

                    // Get the Digital Downloads from the engine
                    kkAppEng.getProductMgr().fetchDigitalDownloads();
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

            String ddIdStr = request.getParameter("ddId");

            if (ddIdStr == null)
            {
                return "MyAccount";
            }

            int ddId;
            try
            {
                ddId = Integer.parseInt(ddIdStr);
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

            // Get the Digital Downloads
            DigitalDownloadIf[] downloads = kkAppEng.getProductMgr().getDigitalDownloads();

            if (downloads == null || downloads.length == 0)
            {
                return "MyAccount";
            }

            for (int i = 0; i < downloads.length; i++)
            {
                DigitalDownloadIf dd = downloads[i];
                if (dd != null && (dd.getId() == ddId) && dd.getProduct() != null)
                {
                    // Get the file
                    String fullPath = kkAppEng.getDdbasePath();
                    if (dd.getFilePath() != null)
                    {
                        fullPath = dd.getFilePath();
                    } else
                    {
                        fullPath = fullPath + FileUtils.FILE_SEPARATOR
                                + dd.getProduct().getFilePath();
                    }
                    fullPath = Utils.removeMultipleSlashes(fullPath);

                    File file = new File(fullPath);
                    if (file.canRead() == false)
                    {
                        throw new Exception("The file " + fullPath + " cannot be opened");
                    }

                    kkContentDisposition = "attachment;filename=\"" + file.getName() + "\"";
                    kkContentType = dd.getProduct().getContentType();
                    kkInputName = new FileInputStream(file);

                    // Update the downloaded count
                    kkAppEng.getEng().updateDigitalDownloadCountById(kkAppEng.getSessionId(),
                            dd.getId());

                    // Get the Digital Downloads from the engine
                    kkAppEng.getProductMgr().fetchDigitalDownloads();
                }
            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

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
     * @return the kkInputName
     */
    public FileInputStream getKkInputName()
    {
        return kkInputName;
    }

    /**
     * @param kkInputName
     *            the kkInputName to set
     */
    public void setKkInputName(FileInputStream kkInputName)
    {
        this.kkInputName = kkInputName;
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

}
