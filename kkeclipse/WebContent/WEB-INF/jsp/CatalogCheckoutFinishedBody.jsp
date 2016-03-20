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
<% com.konakart.appif.OrderIf order = (com.konakart.appif.OrderIf)request.getAttribute("order");%>

 				<h1 id="page-title"><kk:msg  key="checkout.finished.orderprocessed"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="checkout-finished">
			    		<form action="CheckoutFinishedSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="notification-header">
								    Order Number is <%=order.getId() %>.<br>
									Your order has been received by MeatRoot and will be delivered on <%=order.getCustom2() %> between <%if(order.getCustom1().equalsIgnoreCase("m"))%> 7am - 10:30am <%else if(order.getCustom1().equalsIgnoreCase("a")) %> 1pm - 3pm <%else %> 6pm - 8pm. Thank you for shopping with us.
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
							</div>
						</form>
			    	</div>
	    		</div>
	    		
	    		<%if (kkEng.getCustomerMgr().getCurrentCustomer() != null && kkEng.getCustomerMgr().getCurrentCustomer().getType() != 2 && kkEng.getCustomerMgr().getCurrentCustomer().getGlobalProdNotifier() == 0) { %>
	    		
	    		 <div

  id="refcandy-mint"

  data-app-id="ijdpgu342un7laa7kzhijptcs"

  data-fname="<%= kkEng.getCustomerMgr().getCurrentCustomer().firstName %>"

  data-lname="<%= kkEng.getCustomerMgr().getCurrentCustomer().lastName%>"

  data-email="<%= kkEng.getCustomerMgr().getCurrentCustomer().emailAddr%>"

  data-amount="00.00"

  data-currency="INR"

  data-timestamp="1456731616"

  data-external-reference-id="93211001"

  data-signature="c97ca2e54f5e61eec1a8a73940e811c1"

></div>
	    		
	    		<%} %>
	    		
	  <script>


$('#refcandy-mint').attr({
	"data-fname": "",
	"data-lname": "",
	"data-email": "",
	"data-amount": "",
	"data-currency": "",
	"data-timestamp": "1456731616",
	"data-external-reference-id": "93211001",
	"data-signature": "c97ca2e54f5e61eec1a8a73940e811c1"
	
});

</script>

<script>(function(e){var t,n,r,i,s,o,u,a,f,l,c,h,p,d,v;z="script";l="refcandy-purchase-js";c="refcandy-mint";p="go.referralcandy.com/purchase/";t="data-app-id";r={email:"a",fname:"b",lname:"c",amount:"d",currency:"e","accepts-marketing":"f",timestamp:"g","referral-code":"h",locale:"i","external-reference-id":"k",signature:"ab"};i=e.getElementsByTagName(z)[0];s=function(e,t){if(t){return""+e+"="+encodeURIComponent(t)}else{return""}};d=function(e){return""+p+h.getAttribute(t)+".js?aa=75&"};if(!e.getElementById(l)){h=e.getElementById(c);if(h){o=e.createElement(z);o.id=l;a=function(){var e;e=[];for(n in r){u=r[n];v=h.getAttribute("data-"+n);e.push(s(u,v))}return e}();o.src=""+e.location.protocol+"//"+d(h.getAttribute(t))+a.join("&");return i.parentNode.insertBefore(o,i)}}})(document);</script>

	   
