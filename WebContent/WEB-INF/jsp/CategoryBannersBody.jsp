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
<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.appif.MiscItemIf[] miscItems = kkEng.getCategoryMgr().getCurrentCat().getMiscItems();%>
<% boolean hideRow1 =  kkEng.getPropertyAsBoolean("category.page.hide.banner.row1", false);%>
<% boolean hideRow2 =  kkEng.getPropertyAsBoolean("category.page.hide.banner.row2", false);%>

<%if (miscItems != null && miscItems.length > 0){%>
	<%if (!hideRow1) { %>
		<% com.konakart.appif.MiscItemIf banner1 = miscItems[0];%>
		<%if (banner1.getCustom1() != null && banner1.getCustom1().length() > 0){%>
			<a href="<%=banner1.getCustom1()%>"><div id="banner" class="rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/<%=banner1.getItemValue()%>');"></div></a> 	
		<% } else { %>
			<div id="banner" class="rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/<%=banner1.getItemValue()%>');"></div>
		<% } %>	
	<% } %>	
	<%if (!hideRow2) { %>
		<%if (miscItems.length > 2){%>
			<div id="banners">
			<% com.konakart.appif.MiscItemIf banner2 = miscItems[1];%>
			<%if (banner2.getCustom1() != null && banner2.getCustom1().length() > 0){%>
				<a href="<%=banner2.getCustom1()%>"><div class="banner-double rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/<%=banner2.getItemValue()%>');"></div></a> 	
			<% } else { %>
				<div class="banner-double rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/<%=banner2.getItemValue()%>');"></div>
			<% } %>	
			<% com.konakart.appif.MiscItemIf banner3 = miscItems[2];%>
			<%if (banner3.getCustom1() != null && banner3.getCustom1().length() > 0){%>
				<a href="<%=banner3.getCustom1()%>"><div class="banner-small rounded-corners last-child" style="background-image: url('<%=kkEng.getImageBase()%>/<%=banner3.getItemValue()%>');"></div></a> 	
			<% } else { %>
				<div class="banner-small rounded-corners last-child" style="background-image: url('<%=kkEng.getImageBase()%>/<%=banner3.getItemValue()%>');"></div>
			<% } %>	
			</div>
		<% } %>
	<% } %>	
<% } %>

