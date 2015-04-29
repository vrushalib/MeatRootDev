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


<div id="kkfooter">
    <div id="contact-info" class="footer-area narrow">
    	KonaKart<br />
		020 7946 0000<br />
		<a href ="#">info@konakart.com</a>
    </div>
   	<div id="newsletter" class="footer-area wide">
   		<kk:msg  key="footer.subscribe"/><br />
 		<input type="text" id="newsletter-input" class="rounded-corners-left" onblur="onBlur(this)" onfocus="onFocus(this)" value="<kk:msg  key="footer.your.email"/>">
		<a id="newsletter-button" class="rounded-corners-right"><kk:msg  key="footer.signup"/></a>
		<div id="newsletter-msg"></div>
   	</div>
   	<div id="links-1" class="footer-area narrow">
		<a href ="AboutUs.action"><kk:msg  key="footer.about.us"/></a><br />
		<a href ="ShippingAndHandling.action"><kk:msg  key="footer.shipping.and.handling"/></a><br />
		<a href ="Returns.action"><kk:msg  key="footer.returns"/></a><br />
		<a href ="InternationalOrders.action"><kk:msg  key="footer.international.orders"/></a><br />
   	</div>
   	<div id="links-2" class="footer-area narrow">
		<a href ="PrivacyPolicy.action"><kk:msg  key="footer.privacy.policy"/></a><br />
		<a href ="TermsOfUse.action"><kk:msg  key="footer.terms.of.use"/></a><br />
		<a href ="Help.action"><kk:msg  key="footer.help"/></a><br />
		<a href ="ContactUs.action"><kk:msg  key="footer.contact.us"/></a><br />
   	</div>
   	<div id="social" class="footer-area narrow last-child">
		<kk:msg  key="footer.connect"/><br />
		<a href="http://www.twitter.com" target="_blank" class="fa fa-twitter-square"></a>
		<a href="http://www.facebook.com" target="_blank" class="fa fa-facebook-square"></a>
		<a href="http://www.pinterest.com" target="_blank" class="fa fa-pinterest-square"></a>
   	</div>
</div>