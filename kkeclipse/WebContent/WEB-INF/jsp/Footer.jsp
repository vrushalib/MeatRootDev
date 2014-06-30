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
<%@ page import="java.util.List" %>
<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>

<script>
function onBlur(el) {
    if (el.value == '') {
        el.value = el.defaultValue;
    }
}
function onFocus(el) {
    if (el.value == el.defaultValue) {
        el.value = '';
    }
}

$(function() {
	$("#newsletter-input").keydown(function (e){
	    if(e.keyCode == 13){
	    	submitNewsletterForm();
	    }
	});
});		
</script>

<div  class="item-area wide rounded-corners">
<div id="kkfooter">
	<div id="links-1" class="footer-area narrow">
	<h2 class="title">Popular Brand</h2>	
 	<% List<com.konakart.appif.CategoryIf> cats = com.konakart.app.GetCategoryTree.getAllInvisibleCategories();%>
 	<% for (com.konakart.appif.CategoryIf cat : cats) { %>
 	<%String menuClass; %>
	<% menuClass = "menu-item rounded-corners"; %>
     <a href='<%="SelectCat.action?catId="+cat.getId()%>' class="<%=menuClass%>" style="width: auto;"><%=cat.getName()%></a><br />
 	<% } %>
   	<%for (int i = 0; i < catMgr.getCats().length; i++) {%>
		<%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i]; %>
		<%String menuClass; %>
		<%if (i == catMgr.getCats().length-1){ %>
			<% menuClass = "menu-item rounded-corners last-child"; %>
		<% } else { %>
			<% menuClass = "menu-item rounded-corners"; %>
		<% } %>
		<a href='<%="SelectCat.action?catId="+cat.getId()%>' class="<%=menuClass%>" style="width: auto;"><%=cat.getName()%></a><br />
	<% } %>				
		
   	</div>

  
   	<div id="links-1" class="footer-area narrow">
   	<h2 class="title">Popular Categories</h2>
   
 	<% for (com.konakart.appif.CategoryIf cat : cats) { %>
 	<%String menuClass; %>
	<% menuClass = "menu-item rounded-corners"; %>
     <a href='<%="SelectCat.action?catId="+cat.getId()%>' class="<%=menuClass%>" style="width: auto;"><%=cat.getName()%></a><br />
 	<% } %>
   	<%for (int i = 0; i < catMgr.getCats().length; i++) {%>
		<%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i]; %>
		<%String menuClass; %>
		<%if (i == catMgr.getCats().length-1){ %>
			<% menuClass = "menu-item rounded-corners last-child"; %>
		<% } else { %>
			<% menuClass = "menu-item rounded-corners"; %>
		<% } %>
		<a href='<%="SelectCat.action?catId="+cat.getId()%>' class="<%=menuClass%>" style="width: auto;"><%=cat.getName()%></a><br />
	<% } %>				
		
   	</div>
   	<div id="links-2" class="footer-area narrow">
   	<a href ="AboutUs.action"><kk:msg  key="footer.about.us"/></a><br />
   	<a href ="PlacingOrder.action"><kk:msg  key="footer.placing.order"/></a><br />
   	
		<a href ="Payment.action"><kk:msg  key="footer.payment"/></a><br />
		<a href ="DeliveryDetails.action"><kk:msg  key="footer.delivery.details"/></a><br />
		<a href ="OrderTracking.action"><kk:msg  key="footer.order.tracking"/></a><br />
		<a href ="CancellationPolicy.action"><kk:msg  key="footer.cancellation.policy"/></a><br />
		<a href ="ContentChanges.action"><kk:msg  key="footer.content.changes"/></a><br />
		<a href ="TermsOfUse.action"><kk:msg  key="footer.terms.of.use"/></a><br />
		<a href ="FAQ.action"><kk:msg  key="footer.faq"/></a><br />
		<a href ="ContactUs.action"><kk:msg  key="footer.contact.us"/></a><br />
   	</div>
   	<div id="social" class="footer-area narrow last-child">
		<kk:msg  key="footer.connect"/><br />
		<a href="http://www.twitter.com" target="_blank" class="twitter-grey social-icon"></a>
		<a href="http://www.facebook.com" target="_blank" class="facebook-grey social-icon"></a>
		<a href="http://www.pinterest.com" target="_blank" class="pinterest-grey social-icon"></a>
		<a href="https://plus.google.com" target="_blank" class="google-grey social-icon"></a>
   	</div>
</div>
</div>