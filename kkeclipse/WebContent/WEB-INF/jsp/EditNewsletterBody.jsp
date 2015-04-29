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

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>

 				<h1 id="page-title"><kk:msg  key="edit.newsletter.title"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
			    		<form action="EditNewsletterSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="notification-header">
									<div class="newsletter-checkbox">
										<s:checkbox name="newsletterBool"/>
									</div>
									<div class="notification-explanation">										
										<kk:msg  key="edit.newsletter.newslettercontents"/>.
									</div>
								</div>
							</div>
							<div class="form-buttons-wide">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


