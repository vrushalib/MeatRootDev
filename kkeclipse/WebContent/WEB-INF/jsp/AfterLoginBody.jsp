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
<% com.konakart.al.ProductMgr productMgr = kkEng.getProductMgr();%>
<% com.konakart.al.RewardPointMgr rewardPointMgr = kkEng.getRewardPointMgr();%>
<% com.konakart.appif.DigitalDownloadIf[] downloads = productMgr.getDigitalDownloads();%>
<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>

 		<%boolean manageCCAuthNetSIM=kkEng.getConfigAsBoolean("MODULE_PAYMENT_AUTHORIZENET_ENABLE_CIM", false, false); %>
   		<h1 id="page-title"><kk:msg  key="after.login.body.myaccountinfo"/></h1>
	    		<div id="my-account" class="content-area rounded-corners">
	    			<s:if test="hasActionErrors()">
					   <div class="messageStackError">  
					        <s:iterator value="actionErrors">  
					            <s:property escape="false"/>
					        </s:iterator>  
		    			</div>  
					</s:if>		    		    		
    				<s:if test="hasActionMessages()">
					   <div class="messageStackSuccess">  
					        <s:iterator value="actionMessages">  
					            <s:property escape="false"/>
					        </s:iterator>  
		    			</div>  
					</s:if>		    	    		
		    		<div id="my-account-column-left">
	    				<div id="personal-info" class="my-account-area">
	    					<div class="my-account-area-header">
	    						<h3><kk:msg  key="after.login.body.primary.address"/></h3>
	    					</div>	
							<div class="my-account-area-content">
							<%if (cust.getAddresses() != null && cust.getAddresses().length > 0){ %>
								<%=kkEng.removeCData(cust.getAddresses()[0].getFormattedAddress())%>
								<br/><br/><%=cust.getEmailAddr() %>
								<br/><%=cust.getTelephoneNumber() %>
							<% } %>
							</div>	
						</div>	
						<%if (downloads != null && downloads.length > 0){ %>
							<div id="downloads" class="my-account-area">
			    				<div class="my-account-area-header">
			    					<h3><kk:msg  key="after.login.body.downloads"/></h3>	 
			    				</div>
			    				<div class="digital-downloads">
									<table>
										<% for (int i = 0; i < downloads.length; i++){ %>
											<% com.konakart.appif.DigitalDownloadIf download = downloads[i];%>
											<%if (download.getProduct() != null) {%>
											<tr>
												<td ><%=download.getProduct().getName()%></td>
												<td ><%=download.getProduct().getModel()%></td>
												<td ><kk:msg  key="after.login.body.downloaded"/>&nbsp;<%=download.getTimesDownloaded()%>&nbsp;<kk:msg  key="after.login.body.times"/></td>
												<%if (download.getExpirationDate() != null){ %>
													<td  width="80"><%=kkEng.getDateAsString(download.getExpirationDate())%></td>
												<%}%>
												<td  align="right">
													<%if (kkEng.isPortlet()){ %>
														<a href='<%="DigitalDownloadPortlet.action?ddId="+download.getId()%>'>
															<span class="button-small small-rounded-corners"><span><kk:msg  key="common.download"/></span></span>
														</a>
													<%} else {%>
														<a href='<%="DigitalDownload.action?ddId="+download.getId()%>'>
															<span class="button-small small-rounded-corners"><span><kk:msg  key="common.download"/></span></span>
														</a>
													<%}%>
												</td>
											</tr>
											<%}%>
										<%}%>
									</table>
			    				</div>	
			    			</div>	
						<% } %>							
						<%if (cust.getOrders() != null && cust.getOrders().length > 0){ %>	
							<%boolean enableInvoice=kkEng.getConfigAsBoolean("ENABLE_PDF_INVOICE_DOWNLOAD", false); %>	
							<div id="last-orders" class="my-account-area">
			    				<div class="my-account-area-header">
			    					<h3><kk:msg  key="after.login.body.last.orders"/></h3><a href="ShowAllOrders.action" class="my-account-option text-link"><kk:msg  key="after.login.body.showallorders"/></a>	 
			    				</div>	
								<% for (int i = 0; i < cust.getOrders().length; i++){ %>
									<% com.konakart.appif.OrderIf order = cust.getOrders()[i];%>
									<% int numItems = (order.getOrderProducts()!=null)?order.getOrderProducts().length:0;%>
									<% String statusClass = (order.getStatusText()!=null&&order.getStatusText().equalsIgnoreCase("delivered"))?"shipped":"pending";%>
					    			<div class="last-order">
					    				<table>
					    					<tbody>
					    						<tr>
					    							<td>#<%=order.getId()%></td>
					    							<td><%=kkEng.getDateAsString(order.getDatePurchased())%></td>
					    							<td><kk:msg  key="common.total"/>: <%=kkEng.formatPrice(order.getTotalIncTax(),order.getCurrencyCode())%></td>
					    							<td><div class="label <%=statusClass%>"><%=order.getStatusText()%></div></td>
					    							<td class="last-order-icons">
					    								<a class="text-link fa fa-eye order-action" title='<%=kkEng.getMsg("common.view")%>' href='<%="ShowOrderDetails.action?orderId="+order.getId()%>'></a>
					    								<a class="text-link fa fa-repeat order-action" title='<%=kkEng.getMsg("common.repeat")%>' href='<%="RepeatOrder.action?orderId="+order.getId()%>'></a>
					    							<%--	<a class="text-link fa fa-truck order-actio" title='<%=kkEng.getMsg("common.track")%>'></a>	
													<%if (enableInvoice) {%>	
														<%if (kkEng.isPortlet()){ %>
															<a title='<%=kkEng.getMsg("common.invoice")%>' class="text-link fa fa-file-pdf-o order-action" href='<%="DownloadInvoicePortlet.action?orderId="+order.getId()%>'></a>
														<%} else {%>
															<a title='<%=kkEng.getMsg("common.invoice")%>' class="text-link fa fa-file-pdf-o order-action" href='<%="DownloadInvoice.action?orderId="+order.getId()%>'></a>
														<%}%>
													<% } %> --%>
													</td>
					    						</tr>
				    						</tbody>
				    						<%if (order.getOrderProducts() != null && order.getOrderProducts().length > 0){ %>
					    						<tr>
					    							<td colspan="8">	    					
							    						 <table>
									    					<thead>
									    						<tr>
									    							<td class="wide-col"><kk:msg  key="common.item"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.quantity"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.total"/></td>		    						
									    						</tr>
									    					</thead>
									    					<tbody>				    						
							    							<% for (int j = 0; j < order.getOrderProducts().length; j++){ %>	
							    								<% com.konakart.appif.OrderProductIf orderProd = order.getOrderProducts()[j];%>
									    						<tr>
									    							<td><a class="text-link" href='<%="SelectProd.action?prodId="+orderProd.getProductId()%>'><%=orderProd.getName()%></a></td>
									    							<td class="right"><%=orderProd.getQuantity()%></td>
									    							<%if (kkEng.displayPriceWithTax()) {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceIncTax(),order.getCurrencyCode())%></td>
																	<%} else {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceExTax(),order.getCurrencyCode())%></td>
																	<%}%>	
									    						</tr>
							    							<% } %>
									    					</tbody>	    				
									    				</table>
								    				</td>
					    						</tr>
					    					<% } %>		    							    							    							    				
					    				</table> 
					    			</div>	
					    			<div class="last-order-mobile">
					    				<table>
					    					<tbody>
					    						<tr>
					    							<td>#<%=order.getId()%></td>
					    							<td><%=kkEng.getDateAsString(order.getDatePurchased())%></td>
					    							
					    							<td><div class="label <%=statusClass%>"><%=order.getStatusText()%></div></td>
					    							<td class="last-order-icons">
					    								<a class="text-link fa fa-eye order-action" title='<%=kkEng.getMsg("common.view")%>' href='<%="ShowOrderDetails.action?orderId="+order.getId()%>'></a>
					    								<a class="text-link fa fa-repeat order-action" title='<%=kkEng.getMsg("common.repeat")%>' href='<%="RepeatOrder.action?orderId="+order.getId()%>'></a>
					    						<%--		<a class="text-link fa fa-truck order-actio" title='<%=kkEng.getMsg("common.track")%>'></a>	
													<%if (enableInvoice) {%>	
														<%if (kkEng.isPortlet()){ %>
															<a title='<%=kkEng.getMsg("common.invoice")%>' class="text-link fa fa-file-pdf-o order-action" href='<%="DownloadInvoicePortlet.action?orderId="+order.getId()%>'></a>
														<%} else {%>
															<a title='<%=kkEng.getMsg("common.invoice")%>' class="text-link fa fa-file-pdf-o order-action" href='<%="DownloadInvoice.action?orderId="+order.getId()%>'></a>
														<%}%>
													<% } %> --%>
													</td>
					    						</tr>
				    						</tbody>
				    						<%if (order.getOrderProducts() != null && order.getOrderProducts().length > 0){ %>
					    						<tr>
					    							<td colspan="8">	    					
							    						 <table>
									    					<thead>
									    						<tr>
									    							<td class="wide-col"><kk:msg  key="common.item"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.quantity"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.total"/></td>		    						
									    						</tr>
									    					</thead>
									    					<tbody>				    						
							    							<% for (int j = 0; j < order.getOrderProducts().length; j++){ %>	
							    								<% com.konakart.appif.OrderProductIf orderProd = order.getOrderProducts()[j];%>
									    						<tr>
									    							<td><a class="text-link" href='<%="SelectProd.action?prodId="+orderProd.getProductId()%>'><%=orderProd.getName()%></a></td>
									    							<td class="right"><%=orderProd.getQuantity()%></td>
									    							<%if (kkEng.displayPriceWithTax()) {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceIncTax(),order.getCurrencyCode())%></td>
																	<%} else {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceExTax(),order.getCurrencyCode())%></td>
																	<%}%>	
									    						</tr>
							    							<% } %>
									    					</tbody>
									    					<tfoot>
										    					<tr class="total-amount">
										    						<td><kk:msg  key="common.total"/>: </td>
										    						<td></td>
										    						<td><%=kkEng.formatPrice(order.getTotalIncTax(),order.getCurrencyCode())%></td>
										    					</tr>  
									    					</tfoot>  				
									    				</table>
								    				</td>
					    						</tr>
					    					<% } %>		    							    							    							    				
					    				</table> 
					    			</div>																
				    			<% } %>
					    	</div>																	
				    	<% } %>
					</div>	 
					<%if (kkEng.getCustomerMgr().getCurrentCustomer() != null && kkEng.getCustomerMgr().getCurrentCustomer().getType() != com.konakart.bl.CustomerMgr.CUST_TYPE_NON_REGISTERED_CUST) { %>
						<div id="my-account-column-right">
							<div id="addressbook" class="my-account-area">
								<h3><kk:msg  key="after.login.body.personal.information"/></h3>
								<div class="my-account-area-content">
									<a href="EditEmail.action" class="text-link"><kk:msg  key="after.login.body.changeemail"/></a>
									<a href="ChangePassword.action" class="text-link"><kk:msg  key="after.login.body.changepassword"/></a>
									<a href="EditCustomer.action" class="text-link"><kk:msg  key="after.login.body.changeaccountinfo"/></a>
									<a href="AddressBook.action" class="text-link"><kk:msg  key="after.login.body.changeaddrbook"/></a>
									<%if (manageCCAuthNetSIM) { %>
										<a href="AuthorizeNetManageCreditCards.action" id="authNetManageCreditCards" class="text-link"><kk:msg  key="after.login.body.manageCreditCards"/></a>
								    <% } %>
								</div>
							</div>
							<%if (rewardPointMgr.isEnabled()) { %>				
								<%int points = rewardPointMgr.pointsAvailable(); %>						
								<div id="reward-points" class="my-account-area">
									<h3><kk:msg  key="after.login.body.rewardpoints"/></h3>
									<div class="my-account-area-content">
										<span id="reward-point-text"><kk:msg  key="after.login.body.rewardpoints.available" arg0="<%=String.valueOf(points)%>"/>.</span>
										<a href="ShowRewardPoints.action" class="text-link"><kk:msg  key="after.login.body.showrewardpoints"/></a>
									</div>
								</div>
							<% } %>
							<div id="settings" class="my-account-area">
								<h3><kk:msg  key="common.notifications"/></h3>
								<div class="my-account-area-content">
									<a href="EditNewsletter.action" class="text-link"><kk:msg  key="after.login.body.subscribenewsletter"/></a>
									<a href="EditNotifiedProducts.action" class="text-link"><kk:msg  key="after.login.body.changeprodnotlist"/></a>
								</div>
							</div>
							<%if (kkEng.getConfigAsBoolean("ENABLE_GIFT_REGISTRY",false)) {%>						
								<div id="lists" class="my-account-area">
									<h3><kk:msg  key="common.gift.registries"/></h3>
									<div class="my-account-area-content">
										<a href="CreateGiftRegistry.action" class="text-link"><kk:msg  key="after.login.body.createweddinglist"/>.</a>
										<%if (cust.getWishLists() != null && cust.getWishLists().length > 0){ %>
											<% for (int i = 0; i < cust.getWishLists().length; i++){ %>
												<% com.konakart.appif.WishListIf wishList = cust.getWishLists()[i];%>
												<%if (wishList != null && wishList.getListType()!= com.konakart.al.WishListMgr.WISH_LIST_TYPE ) {%>
													<a href='<%="EditGiftRegistry.action?wishListId="+wishList.getId()%>' class="text-link"><kk:msg  key="common.edit"/>&nbsp;<strong><%=wishList.getName()%></strong></a>
												<%}%>
											<%}%>
										<%}%> 					
									</div>
								</div>
							<% } %>
						</div>   	
					<% } %>
	    		</div>
	    		
		    	<s:set scope="request" var="token" value="token"/>
				<% String token = (String)request.getAttribute("token");%> 
				
				<%if (manageCCAuthNetSIM && token != null) { %>

					 <link href="AuthorizeNetCIM/manage.css" rel="stylesheet" type="text/css" />
					 <script type="text/javascript" src="AuthorizeNetCIM/popup.js"></script>
					 
					  <script type="text/javascript">
					  	<%boolean testMode = kkEng.getConfigAsBoolean("MODULE_PAYMENT_AUTHORIZENET_TESTMODE", true); %>
						<%if (testMode) { %>
							// Use https://test.authorize.net
							AuthorizeNetPopup.options.useTestEnvironment = true;
				    	<% } else { %>
				    	 	// Use https://secure.authorize.net
				    		AuthorizeNetPopup.options.useTestEnvironment = false;
				    	<% } %>
						
						$(function() {
							AuthorizeNetPopup.openManagePopup();
						});
					  </script>
					 
					<form method="post" id="formAuthorizeNetPopup" name="formAuthorizeNetPopup" target="iframeAuthorizeNet" style="display:none;">
					  <input type="hidden" name="Token" value="<%=token%>" />
					</form>
					
					<div id="divAuthorizeNetPopup" style="display:none;" class="AuthorizeNetPopupGrayFrameTheme">
					  <div class="AuthorizeNetPopupOuter">
					    <div class="AuthorizeNetPopupTop">
					      <div class="AuthorizeNetPopupClose">
					        <a href="javascript:;" onclick="AuthorizeNetPopup.closePopup();" title="Close"> </a>
					      </div>
					    </div>
					    <div class="AuthorizeNetPopupInner">
					      <iframe name="iframeAuthorizeNet" id="iframeAuthorizeNet" src="AuthorizeNetCIM/empty.html" frameborder="0" scrolling="no"></iframe>
					    </div>
					    <div class="AuthorizeNetPopupBottom">
					      <div class="AuthorizeNetPopupLogo" title="Powered by Authorize.Net"></div>
					    </div>
					  </div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowT"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowR"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowB"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowL"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowTR"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowBR"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowBL"></div>
					  <div class="AuthorizeNetShadow AuthorizeNetShadowTL"></div>
					</div>
										
					<div id="divAuthorizeNetPopupScreen" style="display:none;"></div>
					
			    <% } %>    		