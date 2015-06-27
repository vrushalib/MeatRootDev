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
<%@page import="com.konakart.app.CategoryImageProps"%>
<%@page import="flexjson.JSONDeserializer"%>


<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% ContentIf[] catBanners = null;%>
<% ContentIf[] catSubBanners = null;%>
<% boolean hideRow1 =  kkEng.getPropertyAsBoolean("category.page.hide.banner.row1", false);%>
<% boolean hideRow2 =  kkEng.getPropertyAsBoolean("category.page.hide.banner.row2", false);%>
<% String contentDir = kkEng.getContentImagesDir();%>
<% boolean contentEnabled = kkEng.getContentMgr().isEnabled();%>

<% String custom2 = kkEng.getCategoryMgr().getCurrentCat().getCustom2(); %>

<% if ( null != custom2 && !custom2.isEmpty() ) { %>
<% CategoryImageProps categoryImageProps = new JSONDeserializer<CategoryImageProps>().deserialize(custom2, CategoryImageProps.class);  %>
<div class="leftImg" style="width:100%; height: 250px; background-size:100%; background-image: url(images/banners/categoryBanner/<%=categoryImageProps.getcU() %>); display:inline-table;"></div>
<a href="SelectProd.action?prodId=<%=categoryImageProps.getbLL()%>"><div class="leftImg" style="width:49.5%; height: 140px; background-size: 100%; background-image: url(images/banners/categoryBanner/<%=categoryImageProps.getbLU() %>); display:inline-table;"></div></a>
<a href="SelectProd.action?prodId=<%=categoryImageProps.getbRL()%>"><div class="rightImg" style="width:50%; height: 140px; background-size: 100%; background-image: url(images/banners/categoryBanner/<%=categoryImageProps.getbRU() %>); display:inline-table;"></div></a>
<% } %>

