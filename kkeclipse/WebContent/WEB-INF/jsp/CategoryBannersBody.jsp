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

<%--<% if (!hideRow1) { 
	<%if (contentEnabled) { %>
		<% catBanners = kkEng.getContentMgr().getContentForTypeAndKey(2, 3, kkEng.getCategoryMgr().getCurrentCat().getSearchKey());%>
	<% } else { %>
		<% catBanners = new Content[1];%>
		<% ContentIf banner = new Content();%>
		<% catBanners[0] = banner;%>
	<% } %>
	
	<% if (catBanners.length > 0) { %>
		<% if (catBanners[0].getClickUrl() != null && catBanners[0].getClickUrl().length() > 0) { %>
			<a href="<%=catBanners[0].getClickUrl()%>">
				<picture class="rounded-corners">
					<!--[if IE 9]><video style="display: none;"><![endif]-->
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName1()%>" media="(min-width: 750px)">
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName2()%>" media="(min-width: 440px)">
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName3()%>" >
					<!--[if IE 9]></video><![endif]-->
					<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName1()%>">
				</picture>
			</a> 	
		<% } else { %>
			<picture class="rounded-corners">
				<!--[if IE 9]><video style="display: none;"><![endif]-->
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName1()%>" media="(min-width: 750px)">
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName2()%>" media="(min-width: 440px)">
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName3()%>" >
				<!--[if IE 9]></video><![endif]-->
				<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catBanners[0].getDescription().getName1()%>">
			</picture>
		<% } %>
	<% } %>
<% } %>

<% if (!hideRow2) { %>
	<%if (contentEnabled) { %>
		<% catSubBanners = kkEng.getContentMgr().getContentForTypeAndKey(2, 4, kkEng.getCategoryMgr().getCurrentCat().getSearchKey());%>
	<% } else { %>
		<% catSubBanners = new Content[2];%>
		<% ContentIf banner1 = new Content();%>
		<% ContentIf banner2 = new Content();%>
		<% catSubBanners[0] = banner1;%>
		<% catSubBanners[1] = banner2;%>
	<% } %>

	<% if (catSubBanners.length > 1 && catSubBanners[0] != null  && catSubBanners[1] != null) { %>
		<div id="banners">
		<% if (catSubBanners[0].getClickUrl() != null && catSubBanners[0].getClickUrl().length() > 0) { %>
			<a href="<%=catSubBanners[0].getClickUrl()%>">
				<picture class="banner-double rounded-corners">
					<!--[if IE 9]><video style="display: none;"><![endif]-->
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName1()%>" media="(min-width: 750px)">
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName2()%>" media="(min-width: 440px)">
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName3()%>" >
					<!--[if IE 9]></video><![endif]-->
					<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName1()%>">
				</picture>
			</a> 	
		<% } else { %>
			<picture class="banner-double rounded-corners">
				<!--[if IE 9]><video style="display: none;"><![endif]-->
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName1()%>" media="(min-width: 750px)">
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName2()%>" media="(min-width: 440px)">
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName3()%>" >
				<!--[if IE 9]></video><![endif]-->
				<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[0].getDescription().getName1()%>">
			</picture>
		<% } %>	

		<%if (catSubBanners[1].getClickUrl() != null && catSubBanners[1].getClickUrl().length() > 0) { %>
			<a href="<%=catSubBanners[1].getClickUrl()%>">
				<picture class="banner-double rounded-corners last-child">
					<!--[if IE 9]><video style="display: none;"><![endif]-->
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName1()%>" media="(min-width: 750px)">
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName2()%>" media="(min-width: 440px)">
					<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName3()%>" >
					<!--[if IE 9]></video><![endif]-->
					<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName1()%>">
				</picture>
			</a> 	
		<% } else { %>
			<picture class="banner-double rounded-corners last-child">
				<!--[if IE 9]><video style="display: none;"><![endif]-->
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName1()%>" media="(min-width: 750px)">
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName2()%>" media="(min-width: 440px)">
				<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName3()%>" >
				<!--[if IE 9]></video><![endif]-->
				<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=catSubBanners[1].getDescription().getName1()%>">
			</picture>
		<% } %>	
	<% } %>	
	</div>
<% } %>  --%>

