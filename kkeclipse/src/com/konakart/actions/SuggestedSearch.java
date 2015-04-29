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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.SearchResult;
import com.konakart.app.SuggestedSearchOptions;
import com.konakart.appif.SuggestedSearchItemIf;

/**
 * Used to implement suggested search
 */
public class SuggestedSearch extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String term;

    private SearchResult[] srArray;

    private static final String START_TAG = "<b>";

    private static final String END_TAG = "</b>";

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {

            if (term == null || term.length() == 0)
            {
                return SUCCESS;
            }

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            String maxSsiStr = kkAppEng.getConfig("MAX_NUM_SUGGESTED_SEARCH_ITEMS");
            int maxSsi = 10;
            try
            {
                maxSsi = Integer.parseInt(maxSsiStr);
            } catch (Exception e)
            {
            }

            SuggestedSearchOptions options = new SuggestedSearchOptions();

            options.setLanguageCode(kkAppEng.getLocale().substring(0, 2));
            options.setLimit(maxSsi);
            options.setStartTag(START_TAG);
            options.setEndTag(END_TAG);
            options.setSearchText(term.toLowerCase());
            options.setReturnRichText(true);
            options.setReturnRawText(true);

            SuggestedSearchItemIf[] ssArray = kkAppEng.getEng().getSuggestedSearchItems(
                    kkAppEng.getSessionId(), options);
            if (ssArray != null && ssArray.length > 0)
            {
                srArray = new SearchResult[ssArray.length];
                for (int i = 0; i < ssArray.length; i++)
                {
                    SuggestedSearchItemIf ss = ssArray[i];
                    srArray[i] = new SearchResult(processTermResult(ss.getRichText(), /* rich */
                            true), processTermResult(ss.getRawText(), /* rich */false), ss.getId()
                            + "," + ss.getManufacturerId() + "," + ss.getCategoryId());
                }

            }

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * Method to process Solr Term results. Currently the method unescapes any HTML returned by
     * Solr.
     * 
     * @param result
     * @param rich
     * @return Returns the processed String
     */
    private String processTermResult(String result, boolean rich)
    {
        if (result == null || result.length() == 0 || !result.contains("&"))
        {
            return result;
        }

        result = StringEscapeUtils.unescapeHtml4(result);
        if (rich)
        {
            /*
             * If the search string ends in ampersand, solr will split &amp; to highlight just the &
             * char.
             */
            result = result.replace(END_TAG + "amp;", END_TAG);
        }
        return result;
    }

    /**
     * @param term
     *            the term to set
     */
    public void setTerm(String term)
    {
        this.term = term;
    }

    /**
     * @return the srArray
     */
    public SearchResult[] getSrArray()
    {
        return srArray;
    }

    /**
     * @param srArray
     *            the srArray to set
     */
    public void setSrArray(SearchResult[] srArray)
    {
        this.srArray = srArray;
    }

}
