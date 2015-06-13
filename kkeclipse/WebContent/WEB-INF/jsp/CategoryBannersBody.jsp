<%--
//
// (c) 2012 DS Data Systems UK Ltd, All rights reserved.
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
--%>
<%@include file="Taglibs.jsp" %>

<%@page import="com.konakart.app.Content"%>
<%@page import="com.konakart.app.ContentDescription"%>
<%@page import="com.konakart.appif.ContentIf"%>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% ContentIf[] catBanners = null;%>
<% ContentIf[] catSubBanners = null;%>
<% boolean hideRow1 =  kkEng.getPropertyAsBoolean("category.page.hide.banner.row1", false);%>
<% boolean hideRow2 =  kkEng.getPropertyAsBoolean("category.page.hide.banner.row2", false);%>
<% String contentDir = kkEng.getContentImagesDir();%>
<% boolean contentEnabled = kkEng.getContentMgr().isEnabled();%>

<% String custom1 = kkEng.getCategoryMgr().getCurrentCat().getCustom2(); %>
<% String custom2 = kkEng.getCategoryMgr().getCurrentCat().getCustom3(); %>

<% if ( null != custom1 && !custom1.isEmpty() && null != custom2 && !custom2.isEmpty()) { %>
<% String productId1 = custom1.split(":")[0]; %>
<% String productURL1 = custom1.split(":")[1]; %>
<% String productId2 = custom2.split(":")[0]; %>
<% String productURL2 = custom2.split(":")[1]; %>
<a href="SelectProd.action?prodId=<%=productId1%>"><div class="leftImg" style="width:49%; height: 200px; background-image: url(<%=productURL1 %>); display:inline-table;"></div></a>
<a href="SelectProd.action?prodId=<%=productId2%>"><div class="rightImg" style="width:49%; height: 200px; background-image: url(<%=productURL2 %>); display:inline-table;"></div></a>
<% } %>

