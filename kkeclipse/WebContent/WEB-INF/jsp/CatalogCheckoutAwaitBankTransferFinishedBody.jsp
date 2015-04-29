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
<% com.konakart.al.OrderMgr orderMgr = kkEng.getOrderMgr();%>
<% com.konakart.appif.OrderIf order = orderMgr.getCheckoutOrder();%>

 				<h1 id="page-title"><kk:msg  key="checkout.finished.orderprocessed"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="checkout-finished">
			    		<form action="CheckoutFinishedSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="notification-header">
									<kk:msg  key="checkout.finished.orderprocessedawaitbt"/>.
								</div>
							</div>
							<%if (kkEng.getCustomerMgr().getCurrentCustomer() != null && kkEng.getCustomerMgr().getCurrentCustomer().getType() != 2 && kkEng.getCustomerMgr().getCurrentCustomer().getGlobalProdNotifier() == 0) { %>
								<div class="form-section">										
									<h4><kk:msg  key="checkout.finished.notifyme"/>:</h4>			
									<div class="notification-header">
									<s:set scope="request" var="itemList" value="itemList"/> 
									<% java.util.ArrayList<com.konakart.al.NotifiedProductItem> itemList = (java.util.ArrayList<com.konakart.al.NotifiedProductItem>)request.getAttribute("itemList");%>
									<%if (itemList != null && itemList.size() > 0) { %>
										<%int i = 0; %>
									    <%for (java.util.Iterator<com.konakart.al.NotifiedProductItem> iterator = itemList.iterator(); iterator.hasNext();){%>
											<%com.konakart.al.NotifiedProductItem item =  iterator.next();%>
											<div class="select-notified-prod-section">
												<div class="notification-checkbox <%=(i%2==0)?"even":"odd"%>"><input type="checkbox" name="itemList[<%=i%>].remove" value="true"/></div>
												<div class="notification-explanation <%=(i%2==0)?"even":"odd"%>"><%=item.getProdName()%></div>
												<input type="hidden" name="itemList[<%=i%>].prodId" value="<%=item.getProdId()%>"/>
											</div>
										<%i++;} %>
									<% } %>
								</div>
							<% } %>
							<div class="form-buttons-wide">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>