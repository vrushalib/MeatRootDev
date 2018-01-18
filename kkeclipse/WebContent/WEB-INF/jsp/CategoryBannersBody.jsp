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

<% if (!hideRow1) { %>
	<%if (contentEnabled) { %>
		<% catBanners = kkEng.getContentMgr().getContentForTypeAndKey(2, 3, kkEng.getCategoryMgr().getCurrentCat().getSearchKey());%>
	<% } else { %>
		<% catBanners = new Content[1];%>
		<% ContentIf banner = new Content();%>

		<%if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Computer Peripherals")) { %>
			<% banner.setDescription(new ContentDescription());%>
			<% banner.getDescription().setName1("logitech.png");%>
			<% banner.getDescription().setName2("logitech-medium.png");%>
			<% banner.getDescription().setName3("logitech-small.png");%>
			<% banner.setClickUrl("SelectProd.action?prodId=31");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Games")) { %>
			<% banner.setDescription(new ContentDescription());%>
			<% banner.getDescription().setName1("black-ops-2.png");%>
			<% banner.getDescription().setName2("black-ops-2-medium.png");%>
			<% banner.getDescription().setName3("black-ops-2-small.png");%>
			<% banner.setClickUrl("SelectProd.action?prodId=63");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("DVD Movies")) { %>
			<% banner.setDescription(new ContentDescription());%>
			<% banner.getDescription().setName1("dark-knight.png");%>
			<% banner.getDescription().setName2("dark-knight-medium.png");%>
			<% banner.getDescription().setName3("dark-knight-small.png");%>
			<% banner.setClickUrl("SelectProd.action?prodId=20");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Electronics")) { %>
			<% banner.setDescription(new ContentDescription());%>
			<% banner.getDescription().setName1("kindle-fire-hd.jpg");%>
			<% banner.getDescription().setName2("kindle-fire-hd-medium.jpg");%>
			<% banner.getDescription().setName3("kindle-fire-hd-small.jpg");%>
			<% banner.setClickUrl("SelectProd.action?prodId=34");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Home & Garden")) { %>
			<% banner.setDescription(new ContentDescription());%>
			<% banner.getDescription().setName1("delonghi.png");%>
			<% banner.getDescription().setName2("delonghi-medium.png");%>
			<% banner.getDescription().setName3("delonghi-small.png");%>
			<% banner.setClickUrl("SelectProd.action?prodId=33");%>
		<% } %>
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
		<%if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Computer Peripherals")) { %>
			<% banner1.setDescription(new ContentDescription());%>
			<% banner1.getDescription().setName1("hp-photosmart.png");%>
			<% banner1.getDescription().setName2("hp-photosmart-medium.png");%>
			<% banner1.getDescription().setName3("hp-photosmart-small.png");%>
			<% banner1.setClickUrl("SelectProd.action?prodId=27");%>
			<% banner2.setDescription(new ContentDescription());%>
			<% banner2.getDescription().setName1("deals-of-the-week.png");%>
			<% banner2.getDescription().setName2("deals-of-the-week-medium.png");%>
			<% banner2.getDescription().setName3("deals-of-the-week-small.png");%>
			<% banner2.setClickUrl("");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Games")) { %>
			<% banner1.setDescription(new ContentDescription());%>
			<% banner1.getDescription().setName1("swat-3.png");%>
			<% banner1.getDescription().setName2("swat-3-medium.png");%>
			<% banner1.getDescription().setName3("swat-3-small.png");%>
			<% banner1.setClickUrl("SelectProd.action?prodId=21");%>
			<% banner2.setDescription(new ContentDescription());%>
			<% banner2.getDescription().setName1("winter-deals.png");%>
			<% banner2.getDescription().setName2("winter-deals-medium.png");%>
			<% banner2.getDescription().setName3("winter-deals-small.png");%>
			<% banner2.setClickUrl("");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("DVD Movies")) { %>
			<% banner1.setDescription(new ContentDescription());%>
			<% banner1.getDescription().setName1("harry-potter.png");%>
			<% banner1.getDescription().setName2("harry-potter-medium.png");%>
			<% banner1.getDescription().setName3("harry-potter-small.png");%>
			<% banner1.setClickUrl("SelectProd.action?prodId=11");%>
			<% banner2.setDescription(new ContentDescription());%>
			<% banner2.getDescription().setName1("movie-deals.png");%>
			<% banner2.getDescription().setName2("movie-deals-medium.png");%>
			<% banner2.getDescription().setName3("movie-deals-small.png");%>
			<% banner2.setClickUrl("");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Electronics")) { %>
			<% banner1.setDescription(new ContentDescription());%>
			<% banner1.getDescription().setName1("xbox.png");%>
			<% banner1.getDescription().setName2("xbox-medium.png");%>
			<% banner1.getDescription().setName3("xbox-small.png");%>
			<% banner1.setClickUrl("");%>
			<% banner2.setDescription(new ContentDescription());%>
			<% banner2.getDescription().setName1("electronics-sale.png");%>
			<% banner2.getDescription().setName2("electronics-sale-medium.png");%>
			<% banner2.getDescription().setName3("electronics-sale-small.png");%>
			<% banner2.setClickUrl("");%>
		<% } else if (kkEng.getCategoryMgr().getCurrentCat().getSearchKey().equals("Home & Garden")) { %>
			<% banner1.setDescription(new ContentDescription());%>
			<% banner1.getDescription().setName1("rotak-40.png");%>
			<% banner1.getDescription().setName2("rotak-40-medium.png");%>
			<% banner1.getDescription().setName3("rotak-40-small.png");%>
			<% banner1.setClickUrl("SelectProd.action?prodId=39");%>
			<% banner2.setDescription(new ContentDescription());%>
			<% banner2.getDescription().setName1("gifts-for-the-home.png");%>
			<% banner2.getDescription().setName2("gifts-for-the-home-medium.png");%>
			<% banner2.getDescription().setName3("gifts-for-the-home-small.png");%>
			<% banner2.setClickUrl("");%>
		<% } %>

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
<% } %>

