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



<div id="footer-area" class="footer-area wide rounded-corners" >
<div id="kkfooter">
	<div id="links-1" class="footer-area narrow">
	<h3 class="title">Popular Categories</h3>	
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

    <% com.konakart.appif.ProductIf[] prods = kkEng.getProductMgr().getCustomProducts1(); %>
    <% if(prods != null && prods.length > 0){ %>
   	<div id="links-1" class="footer-area narrow">
	<h3 class="title">Popular Products</h3>
		<%for (int i = 0; i < prods.length; i++) {%>
		<%String menuClass; %>
		<%if (i == prods.length-1){ %>
			<% menuClass = "menu-item rounded-corners last-child"; %>
		<% } else { %>
			<% menuClass = "menu-item rounded-corners"; %>
		<% } %>
		<a href='<%="SelectProd.action?prodId="+prods[i].getId()%>' class="<%=menuClass%>" style="width: auto;"><%=prods[i].getName()%></a><br />
	  <% } %>				
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
   	<h4 class="title" style="margin-top: 6px;">Payment Methods</h4>
   	
  			 	<ul>
                	<li ><img src="<%=kkEng.getImageBase()%>/icons/visa2.jpg" alt="Visa card" /></li>
                    <li><img src="<%=kkEng.getImageBase()%>/icons/masterCard2.jpg" alt="Master Card" /></li>
                    <li><img src="<%=kkEng.getImageBase()%>/icons/netBanking2.jpg" alt="" /></li>
                    <li><img src="<%=kkEng.getImageBase()%>/icons/cardOnDel2.jpg" alt="" /></li>
                    <li><img src="<%=kkEng.getImageBase()%>/icons/cashOnDel2.jpg" alt="" /></li>
                </ul><br/>
		<kk:msg  key="footer.connect"/><br />
		<a href="http://www.twitter.com" target="_blank" class="twitter-grey social-icon"></a>
		<a href="http://www.facebook.com" target="_blank" class="facebook-grey social-icon"></a>
		<a href="http://www.pinterest.com" target="_blank" class="pinterest-grey social-icon"></a>
		<a href="https://plus.google.com" target="_blank" class="google-grey social-icon"></a>
   	</div>
</div>
</div>