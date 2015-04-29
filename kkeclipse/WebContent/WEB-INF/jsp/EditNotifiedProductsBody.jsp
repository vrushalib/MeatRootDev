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

 				<h1 id="page-title"><kk:msg  key="edit.notifiedproducts.body.prodnots"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="product-notifications">
			    		<form action="EditNotifiedProductsSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="notification-header">
									<kk:msg  key="edit.notifiedproducts.body.info"/>.
								</div>
							</div>
							<div class="form-section">							
								<div class="form-section-title">
									<h3><kk:msg  key="edit.notifiedproducts.body.gprodnots"/></h3>							
								</div>
								
								<div class="notification-header">
									<div class="notification-checkbox">
										<s:checkbox name="globalNotificationBool"/>
									</div>
									<div class="notification-explanation">										
										<kk:msg  key="edit.notifiedproducts.body.recprodnots"/>.
									</div>
								</div>
							</div>
							<%if (cust.getGlobalProdNotifier() < 1) { %>
								<div class="form-section">	
									<div class="form-section-title">
										<h3><kk:msg  key="edit.notifiedproducts.body.prodnots"/></h3>	
									</div>
									<%if (cust.getProductNotifications() != null && cust.getProductNotifications().length > 0){ %>	
										<h4><kk:msg  key="edit.notifiedproducts.body.remprodnots"/>.</h4>			
										<div class="notification-header">
										<% for (int i = 0; i < cust.getProductNotifications().length; i++){ %>
						                	<%com.konakart.appif.ProductIf prod = cust.getProductNotifications()[i];%>
						                	<div class="select-notified-prod-section">
												<div class="notification-checkbox <%=(i%2==0)?"even":"odd"%>"><input type="checkbox" name="itemList[<%=i%>].remove" value="true"/></div>
												<div class="notification-explanation <%=(i%2==0)?"even":"odd"%>"><%=prod.getName()%></div>
											</div>
											<input type="hidden" name="itemList[<%=i%>].prodId" value="<%=prod.getId()%>"/>
											<input type="hidden" name="itemList[<%=i%>].prodName" value="<%=prod.getName()%>"/>
										<% } %>
										</div>
									<% } else { %>
										<div class="notification-header">
											<kk:msg  key="edit.notifiedproducts.body.noprodnots"/>.
										</div>
									<% } %>	
								</div>
							<% } %>	
							<div class="form-buttons-wide">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.save"/></span></a>
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


