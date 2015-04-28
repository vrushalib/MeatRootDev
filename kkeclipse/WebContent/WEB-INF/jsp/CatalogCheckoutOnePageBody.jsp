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
<% com.konakart.al.RewardPointMgr rewardPointMgr = kkEng.getRewardPointMgr();%>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>
<% boolean isMultivendor = kkEng.isMultiVendor() && order.getVendorOrders() != null && order.getVendorOrders().length > 0;%>


<script type="text/javascript">	


var onePageRefreshCallback = function(result, textStatus, jqXHR) {
	if (result.timeout != null) {
		alert('<%=kkEng.getMsg("common.session.timeout")%>');
		document.getElementById('form1').submit();
	}else{		
		if (result.storeId == null) {
			$("#ot-table").empty();
			var opArray = result.order.orderProducts;
			if (opArray != null && opArray.length > 0) {
				for ( var i = 0; i < opArray.length; i++) {
					var op = opArray[i];
					$("#ot-table").append(getOrderProductRow(result, op));				
				}	
			}	
			
			var otArray = result.order.orderTotals;
			if (otArray != null && otArray.length > 0) {
				for ( var i = 0; i < otArray.length; i++) {
					var ot = otArray[i];
					$("#ot-table").append(getOrderTotalRow(ot));				
				}	
			}			
		} else {
			// Multi vendor mode
			for ( var i = 0; i < result.order.vendorOrders.length; i++) {
				var order = result.order.vendorOrders[i];
				$("#ot-table_"+order.storeId).find('tr').slice(0,-1).remove();
								
				var otArray = order.orderTotals;
				if (otArray != null && otArray.length > 0) {
					for ( var j = 0; j < otArray.length; j++) {
						var ot = otArray[j];
						if (ot.className.substr(0,6) == "ot_tax") {
							$("#ot-table_"+order.storeId).prepend(getOrderTotalRow(ot));	
						}
					}	
				}	
				
				var opArray = order.orderProducts;
				if (opArray != null && opArray.length > 0) {
					for ( var j = opArray.length-1; j > -1; j--) {
						var op = opArray[j];
						$("#ot-table_"+order.storeId).prepend(getOrderProductRow(result, op));				
					}	
				}
				
				if (order.shippingQuote != null) {
					var shippingCost;
					if (result.displayPriceWithTax) {
						shippingCost = order.shippingQuote.formattedTotalIncTax;
					} else {
						shippingCost = order.shippingQuote.formattedTotalExTax;
					}
					$("#shipping_price_"+order.storeId).html(shippingCost);
					$("#shipping_name_"+order.storeId).html(order.shippingQuote.title+":");		
				}
			}
			
			$("#ot-table").empty();
			var otArray = result.order.orderTotals;
			if (otArray != null && otArray.length > 0) {
				for ( var i = 0; i < otArray.length; i++) {
					var ot = otArray[i];
					$("#ot-table").append(getOrderTotalRow(ot));				
				}	
			}			
		}
		
		if (result.formattedDeliveryAddr != null) {
			$("#formattedDeliveryAddr").html(result.formattedDeliveryAddr);
			$("#editDelivery").attr("href", "EditAddr.action?addrId="+result.deliveryAddrId+"&opcdelivery=true");
		}	
		if (result.formattedBillingAddr != null) {
			$("#formattedBillingAddr").html(result.formattedBillingAddr);
			$("#editBilling").attr("href", "EditAddr.action?addrId="+result.billingAddrId+"&opcbilling=true");
		}
		
		if ($("#couponCodeUpdate").length) $('#couponCodeUpdate').hide();
		if ($("#giftCertCodeUpdate").length) $('#giftCertCodeUpdate').hide();
		if ($("#rewardPointsUpdate").length) $('#rewardPointsUpdate').hide();
		if (!result.otValid) {
			openErrorDialog();
		}else{
			$('#continue-button').show();
		}
		
		if (result.paymentMethods != null) {
			var txt = "";
			for ( var i = 0; i < result.paymentMethods.length; i++) {
				var nv = result.paymentMethods[i];
				if (i==0) {
					txt += '<option  value="'+nv.name+'" selected="selected">'+nv.value+'</option>';
				} else {
					txt += '<option  value="'+nv.name+'" >'+nv.value+'</option>';
				}
			}			
			$('#paymentDetails').html(txt);
		}
	}
};

function getOrderTotalRow(ot) {	
	var rowClass = "costs-and-promotions";
	if (ot.className == "ot_total") rowClass = "shopping-cart-total";
	var row = '<tr class="'+rowClass+'">';
	if (ot.className == "ot_reward_points") {
		row += '<td class="cost-overview">'+ot.title+'</td>';	
		row += '<td class="cost-overview-amounts right">'+ot.value+'</td>';
	} else if (ot.className == "ot_free_product") {
		row += '<td class="cost-overview">'+ot.title+'</td>';	
		row += '<td class="cost-overview-amounts right">'+ot.text+'</td>';
	} else if (ot.className == "ot_total") {
		row += '<td>'+ot.title+'</td>';	
		row += '<td class="right">'+ot.value+'</td>';
	} else if (isDiscountModule(ot.className)) {
		row += '<td class="cost-overview"><span class="discount">'+ot.title+'</span></td>';	
		row += '<td class="cost-overview-amounts right"><span class="discount">-'+ot.value+'</span></td>';
	} else  {
		row += '<td class="cost-overview">'+ot.title+'</td>';	
		row += '<td class="cost-overview-amounts right">'+ot.value+'</td>';
	}
	row += '</tr>';
	return row;
}

function isDiscountModule(module) {
	if (module != null
            && (module=="ot_product_discount" || module=="ot_total_discount"
            || module=="ot_buy_x_get_y_free"  || module=="ot_gift_certificate"
            || module=="ot_redeem_points"     || module=="ot_shipping_discount"))
    {
        return true;
    }
    return false;
}

function getOrderProductRow(result, op) {	
 	var row = '<tr><td>';
 	row +='<a href="'+ getURL("SelectProd.action", new Array("prodId",op.productId)) + '"  class="text-link">'+op.name;
   	if (op.opts != null) {
		for (var k = 0; k < op.opts.length; k++) {
			var opt = op.opts[k];
			if (opt != null) {
				if (opt.type == 0) { // Simple options
					row += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.value+'</span>';
				} else if (opt.type == 1) { // Variable quantity
					row += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.quantity+' '+opt.value+'</span>';
				} else if (opt.type == 2) { // Customer price
					row += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.formattedCustPrice+'</span>';
				} else if (opt.type == 3) { // Customer text
					row += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.customerText+'</span>';
				}					
			}						
		}
	}				 	
   	row +='</a>';
   	row +='<div class="item-quantity">'+result.qtyMsg+': '+ op.quantity+'</div>';				 	
   	row += '</td>';
	if (result.displayPriceWithTax) {
		row += '<td  class="total-price right">';
		row += op.formattedFinalPriceIncTax;
		row += '</td>';
	} else {
		row += '<td  class="total-price right">';
		row += op.formattedFinalPriceExTax;
		row += '</td>';
	}
	row += '</tr>';		
	return row
}

function shippingRefresh(storeId) {
	if (storeId==null) {
		var quotes = document.getElementById("shippingQuotes");
		var selectedQuote = quotes.options[quotes.selectedIndex].value;
		callAction(new Array("shipping",selectedQuote), onePageRefreshCallback, "OnePageRefresh.action");
		setLoading();
	} else {
		var quotes = document.getElementById("shipping_"+storeId);
		var selectedQuote = quotes.options[quotes.selectedIndex].value;
		callAction(new Array("shipping",selectedQuote, "storeId",storeId), onePageRefreshCallback, "OnePageRefresh.action");
		setLoading();	
	}
}

function paymentRefresh() {	
	var paymentDetails = document.getElementById("paymentDetails");
	var selectedPayment = paymentDetails.options[paymentDetails.selectedIndex].value;
	callAction(new Array("payment",selectedPayment), onePageRefreshCallback, "OnePageRefresh.action");
	setLoading();
}

function couponCodeRefresh() {	
	var val = document.getElementById("couponCode").value;
	callAction(new Array("couponCode",val), onePageRefreshCallback, "OnePageRefresh.action");
	setLoading();
}

function giftCertCodeRefresh() {	
	var val = document.getElementById("giftCertCode").value;
	callAction(new Array("giftCertCode",val), onePageRefreshCallback, "OnePageRefresh.action");
	setLoading();
}

function rewardPointsRefresh() {	
	var val = document.getElementById("rewardPoints").value;
	callAction(new Array("rewardPoints",val), onePageRefreshCallback, "OnePageRefresh.action");
	setLoading();
}

function setLoading() {
	$("#ot-table").empty().append('<tr><td colspan="3" class="loading"></td></tr>');	
}

var deliveryAddr = true;

function selectAddr(id) {	
	$("#addr-dialog").dialog('close');
	if (deliveryAddr) {
		setLoading();
		return redirect(getURL("CheckoutNewDeliveryAddr.action", new Array("deliveryId",id)));
	} else {
		callAction(new Array("billingAddrId",id), onePageRefreshCallback, "OnePageRefresh.action");
	}
	setLoading();
}

function openErrorDialog() {
	$('#continue-button').hide();
	$("#error-dialog").dialog('open');
}

function closeErrorDialog() {
	$("#error-dialog").dialog('close');
}



$(function() {
	
    if ($("#form1").length) {
		$("#form1").validate(validationRules);
	}
	
	$("#addr-dialog").dialog({
		autoOpen: false,
		width: "90%",
		modal: "true",
		hide: "blind",
		open: function( event, ui ) {
			var width = $( "#addr-dialog" ).width();
			if (width > 500) {
				$( "#addr-dialog" ).dialog( "option", "width", 500 );
			}
		}
	});
	
	$("#error-dialog").dialog({
		autoOpen: false,
		width: "90%",
		modal: "true",
		hide: "blind"
	});
	
	<s:set scope="request" var="otValid" value="otValid"/> 
	<%Boolean otValid = (Boolean)(request.getAttribute("otValid")); %>
	<%if (!otValid){ %>
		openErrorDialog();
	<%}%>
	
	$('#couponCode').keyup(function() {
		  var elem = $(this);
		  var val = elem.valid();
		  if (val==1 || elem.val()=="") {
			  $('#couponCodeUpdate').show();
		  } else {
			  $('#couponCodeUpdate').hide();
		  }
	});
	

	$('#giftCertCode').keyup(function() {
		  var elem = $(this);
		  var val = elem.valid();
		  if (val==1 || elem.val()=="") {
			  $('#giftCertCodeUpdate').show();
		  } else {
			  $('#giftCertCodeUpdate').hide();
		  }
	});


	$('#rewardPoints').keyup(function() {
		  var elem = $(this);
		  var val = elem.valid();
		  if (val==1 || elem.val()=="") {
			  $('#rewardPointsUpdate').show();
		  } else {
			  $('#rewardPointsUpdate').hide();
		  }
	});

	$("#abdelivery").click(function() {
		deliveryAddr = true;
		$("#addr-dialog").dialog( "open" );
		return false;
	});
	
	$("#abshipping").click(function() {
		deliveryAddr = false;
		$("#addr-dialog").dialog( "open" );
		return false;
	});
	
});

</script>



    		<h1 id="page-title"><kk:msg  key="checkout.confirmation.orderconfirmation"/></h1>
	    		<div id="order-confirmation" class="content-area rounded-corners">
		    		<form action="CheckoutConfirmationSubmit.action" id="form1" method="post" class="form-section">
		    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
		    			<div id="order-confirmation-column-left">
		    				<div id="delivery-address" class="order-confirmation-area">
			    				<div class="heading-container">
			    					<h3><kk:msg  key="show.order.details.body.deliveryaddress"/></h3>
			    					<div class="order-confirmation-options">
			    					<a href="NewAddr.action?opcdelivery=true" title='<%=kkEng.getMsg("checkout.confirmation.new.addr.tip")%>' class="order-confirmation-option text-link has-tooltip"><kk:msg  key="common.new"/></a>
			    					<span class="separator-small"></span>
			    					<a id="editDelivery" href='<%="EditAddr.action?addrId="+order.getDeliveryAddrId()+"&opcdelivery=true"%>' title='<%=kkEng.getMsg("checkout.confirmation.edit.addr.tip")%>' class="order-confirmation-option text-link has-tooltip"><kk:msg  key="common.edit"/></a>
									<%if (cust != null && cust.getType() != 2) { %>
				    					<span class="separator-small"></span>
				    					<a id="abdelivery" title='<%=kkEng.getMsg("checkout.confirmation.addr.book.tip")%>' class="order-confirmation-option text-link has-tooltip"><kk:msg  key="checkout.confirmation.addr.book"/></a>
									<% } %>
			    				</div>
			    				</div>
			    				<div class="order-confirmation-area-content">
				    				<span id="formattedDeliveryAddr"><%=kkEng.removeCData(order.getDeliveryFormattedAddress())%></span>
				    				<%if (!isMultivendor){ %>
										<div id="shipping-info" class="order-confirmation-area-content-select">
											<label><kk:msg  key="show.order.details.body.shippingmethod"/></label>
											<select name="shipping" onchange="javascript:shippingRefresh();" id="shippingQuotes">
												<%if (orderMgr.getShippingQuotes() != null && orderMgr.getShippingQuotes().length > 0){ %>										
													<% String shipping = (order.getShippingQuote()!=null)?order.getShippingQuote().getCode():"";%> 
													<% for (int i = 0; i < orderMgr.getShippingQuotes().length; i++){ %>
														<% com.konakart.appif.ShippingQuoteIf quote = orderMgr.getShippingQuotes()[i];%>
														<%if (shipping.equals(quote.getCode())){ %>
															<option  value="<%=quote.getCode()%>" selected="selected"><%=quote.getDescription()%></option>
														<% } else { %>
															<option  value="<%=quote.getCode()%>"><%=quote.getDescription()%></option>
														<% } %>
													<% } %>										
												<%} else {%>
													<option  value="-1" selected="selected"><kk:msg  key="one.page.checkout.no.shipping.methods"/></option>
												<% } %>
											</select>
										</div>
									<% } %>
								</div>		    				
			    			</div>
			    			<div id="billing-address" class="order-confirmation-area">
			    				<div class="heading-container">
			    					<h3><kk:msg  key="show.order.details.body.billingaddress"/></h3>
			    					<div class="order-confirmation-options">
			    					<a href="NewAddr.action?opcbilling=true" title='<%=kkEng.getMsg("checkout.confirmation.new.addr.tip")%>' class="order-confirmation-option text-link has-tooltip"><kk:msg  key="common.new"/></a>
			    					<span class="separator-small"></span>
			    					<a id="editBilling" href='<%="EditAddr.action?addrId="+order.getBillingAddrId()+"&opcbilling=true"%>' title='<%=kkEng.getMsg("checkout.confirmation.edit.addr.tip")%>' class="order-confirmation-option text-link has-tooltip"><kk:msg  key="common.edit"/></a>
									<%if (cust != null && cust.getType() != 2) { %>
				    					<span class="separator-small"></span>
				    					<a id="abshipping" title='<%=kkEng.getMsg("checkout.confirmation.addr.book.tip")%>' class="order-confirmation-option text-link has-tooltip"><kk:msg  key="checkout.confirmation.addr.book"/></a>
									<% } %>
			    				</div>
			    				</div>
			    				<div class="order-confirmation-area-content">
			    					<span id="formattedBillingAddr"><%=kkEng.removeCData(order.getBillingFormattedAddress())%></span>
									<div id="payment-method" class="order-confirmation-area-content-select">
										<label><kk:msg  key="show.order.details.body.paymentmethod"/></label>
										<select name="payment" onchange="javascript:paymentRefresh();" id="paymentDetails">
											<%if (orderMgr.getPaymentDetailsArray() != null && orderMgr.getPaymentDetailsArray().length > 0){ %>										
												<s:set scope="request" var="payment"  value="payment"/> 						
												<% String payment = ((String)request.getAttribute("payment"));%> 
												<% for (int i = 0; i < orderMgr.getPaymentDetailsArray().length; i++){ %>
													<% com.konakart.appif.PaymentDetailsIf pd = orderMgr.getPaymentDetailsArray()[i];%>
													<%if (payment.equals(pd.getCode())){ %>
														<option  value="<%=pd.getCode()%>" selected="selected"><%=pd.getDescription()%></option>
													<% } else { %>
														<option  value="<%=pd.getCode()%>"><%=pd.getDescription()%></option>
													<% } %>
												<% } %>										
											<%} else {%>
												<option  value="-1" selected="selected"><kk:msg  key="one.page.checkout.no.payment.methods"/></option>
											<% } %>
										</select>
									</div>
									<div id="promotion-codes">
										<div id="promotion-codes-container">
									    	<%if (kkEng.getConfigAsBoolean("DISPLAY_COUPON_ENTRY",false)) { %>
									    		<div class="promotion-codes-field">				
													<label><kk:msg  key="checkout.common.couponcode"/></label>
													<input type="text" name="couponCode" id="couponCode" value="<s:property value="couponCode" />"/>
													<a id="couponCodeUpdate" class="update-button small-rounded-corners" onclick="couponCodeRefresh();" onmouseover="resetGoToCheckout()"><kk:msg  key="common.update"/></a>
													<span class="validation-msg"></span>
												</div>
											<% } %>
											<%if (kkEng.getConfigAsBoolean("DISPLAY_GIFT_CERT_ENTRY",false)) { %>
												<div class="promotion-codes-field">				
													<label><kk:msg  key="checkout.common.giftcertcode"/></label>
													<input type="text" name="giftCertCode" id="giftCertCode" value="<s:property value="giftCertCode" />"/>
													<a id="giftCertCodeUpdate" class="update-button small-rounded-corners" onclick="giftCertCodeRefresh();" onmouseover="resetGoToCheckout()"><kk:msg  key="common.update"/></a>
													<span class="validation-msg"></span>
												</div>
											<% } %>
											<%if (kkEng.getConfigAsBoolean("ENABLE_REWARD_POINTS",false)) { %>
												<%int points = rewardPointMgr.pointsAvailable(); %>
												<%if  (points > 0) { %>
													<div class="promotion-codes-field">	
														<label><kk:msg  key="checkout.common.reward_points" arg0="<%=Integer.toString(points)%>"/></label>
														<input type="text" name="rewardPoints" id="rewardPoints" value="<s:property value="rewardPoints" />"/>
														<a id="rewardPointsUpdate" class="update-button small-rounded-corners" onclick="rewardPointsRefresh();" onmouseover="resetGoToCheckout()"><kk:msg  key="common.update"/></a>
														<span class="validation-msg"></span>
													</div>
												<% } %>
											<% } %>
										</div>
		    						</div>
								</div>		    				
			    			</div>
			    			<div id="delivery-notes" class="order-confirmation-area">
			    				<div class="heading-container">
			    					<h3><kk:msg  key="checkout.common.delivery.notes"/></h3>
			    				</div>
			    				<div class="order-confirmation-area-content">
									<label><kk:msg  key="checkout.common.info"/></label> <textarea rows="5" name="comment"></textarea>
								</div>		    				
			    			</div>
		    			</div>
		    			<div id="order-confirmation-column-right">
			    			<div id="shopping-cart">
			    				<div class="heading-container">
			    					<h3><kk:msg  key="checkout.common.shopping.cart"/></h3>
			    				</div>
			    				<%if (isMultivendor){ %>
			    					<% for (int i = 0; i < order.getVendorOrders().length; i++){ %>
										<% com.konakart.appif.OrderIf vOrder = order.getVendorOrders()[i];%>
										<h3><%=vOrder.getStoreName()%></h3>
				    					<table>
					    					<thead>
					    						<tr>
					    							<td class="wide-col"><kk:msg  key="common.item"/></td>
					    							<td class="narrow-col right"><kk:msg  key="common.total"/></td>
					    						</tr>
					    					</thead>
					    					<tbody id="ot-table_<%=vOrder.getStoreId()%>">
				   								<%if (vOrder.getOrderProducts() != null && vOrder.getOrderProducts().length > 0){ %>
													<% for (int j = 0; j < vOrder.getOrderProducts().length; j++){ %>
														<% com.konakart.appif.OrderProductIf op = vOrder.getOrderProducts()[j];%>
														<tr>
															<td>
							    								<a href='<%="SelectProd.action?prodId="+op.getProductId()%>'  class="text-link"><%=op.getName()%>
																	<kk:prodOptions options="<%=op.getOpts()%>"/>																		
																</a>
																<div class="item-quantity"><kk:msg  key="common.quantity"/>: <%=op.getQuantity()%></div>
															</td>
															<%if (kkEng.displayPriceWithTax()) {%>
																<td  class="total-price right"><%=kkEng.formatPrice(op.getFinalPriceIncTax())%></td>
															<%} else {%>
																<td  class="total-price right"><%=kkEng.formatPrice(op.getFinalPriceExTax())%></td>
															<%}%>	
														</tr>
													<%}%>
												<%}%>					    					
												<%if (vOrder.getOrderTotals() != null && vOrder.getOrderTotals().length > 0){ %>
													<% for (int j = vOrder.getOrderTotals().length-1; j > -1; j--){ %>
														<% com.konakart.appif.OrderTotalIf ot = vOrder.getOrderTotals()[j];%>
														<%if (ot.getClassName().equals("ot_shipping")){%>
															<tr>
																<td>
																	<label id="shipping_name_<%=vOrder.getStoreId()%>"><%=ot.getTitle()%></label><br/>
																	<%com.konakart.appif.ShippingQuoteIf[] quotes = orderMgr.getVendorShippingQuoteMap().get(vOrder.getStoreId()); %>
																	<%if (quotes != null && quotes.length > 1){ %>										
																		<select style='width:100%;' name="shipping_<%=vOrder.getStoreId()%>" onchange="javascript:shippingRefresh('<%=vOrder.getStoreId()%>');" id="shipping_<%=vOrder.getStoreId()%>">
																			<% String shipping = (vOrder.getShippingQuote()!=null)?vOrder.getShippingQuote().getCode():"";%> 
																			<% for (int k = 0; k < quotes.length; k++){ %>
																				<% com.konakart.appif.ShippingQuoteIf quote = quotes[k];%>
																				<%if (shipping.equals(quote.getCode())){ %>
																					<option  value="<%=quote.getCode()%>" selected="selected"><%=quote.getDescription()%></option>
																				<% } else { %>
																					<option  value="<%=quote.getCode()%>"><%=quote.getDescription()%></option>
																				<% } %>
																			<% } %>										
																		</select>
																	<% } %>
																</td>
																<td  class="total-price right" id="shipping_price_<%=vOrder.getStoreId()%>"><%=kkEng.formatPrice(ot.getValue())%></td>
															</tr>													
														<%}else if (kkEng.isTaxModule(ot.getClassName())) {%>
															<tr>
															    <td class="cost-overview"><%=ot.getTitle()%></td>	
																<td class="cost-overview-amounts right"><%=kkEng.formatPrice(ot.getValue())%></td>
															</tr>
														<%}%>		    																		
													<%}%>
												<%}%>
											</tbody>
					    				</table>
					    			<% } %>
				    				<table>
				    					<tbody id="ot-table">											
											<% for (int j = 0; j < order.getOrderTotals().length; j++){ %>
												<% com.konakart.appif.OrderTotalIf ot = order.getOrderTotals()[j];%>
												<%String rowClass = "costs-and-promotions";%>
												<%if (ot.getClassName().equals("ot_total")){ %>
													<%rowClass = "shopping-cart-total";%>
												<% } %>										
												<tr class="<%=rowClass%>">															
													<%if (ot.getClassName().equals("ot_reward_points")){%>
													    <td class="cost-overview"><%=ot.getTitle()%></td>	
														<td class="cost-overview-amounts right"><%=ot.getValue()%></td>
													<%}else if (ot.getClassName().equals("ot_free_product")) {%>
														<td class="cost-overview"><%=ot.getTitle()%></td>
														<td class="cost-overview-amounts right"><%=ot.getText()%></td>
													<%}else if (ot.getClassName().equals("ot_total")) {%>
														<td ><%=ot.getTitle()%></td>
														<td class="right"><%=kkEng.formatPrice(ot.getValue())%></td>
													<%}else if (kkEng.isDiscountModule(ot.getClassName())) {%>	
													    <td class="cost-overview"><span class="discount"><%=ot.getTitle()%></span></td>
														<td class="cost-overview-amounts right"><span class="discount">-<%=kkEng.formatPrice(ot.getValue())%></span></td>
													<%}else{%>
													    <td class="cost-overview"><%=ot.getTitle()%></td>	
														<td class="cost-overview-amounts right"><%=kkEng.formatPrice(ot.getValue())%></td>
													<%}%>		    																		
												</tr>
											<%}%>																						
				    					</tbody>
				    				</table>
			    				<% } else { %>
				    				<table>
				    					<thead>
				    						<tr>
				    							<td class="wide-col"><kk:msg  key="common.item"/></td>
				    							<td class="narrow-col right"><kk:msg  key="common.total"/></td>
				    						</tr>
				    					</thead>
				    					<tbody id="ot-table">
			   								<%if (order.getOrderProducts() != null && order.getOrderProducts().length > 0){ %>
												<% for (int i = 0; i < order.getOrderProducts().length; i++){ %>
													<% com.konakart.appif.OrderProductIf op = order.getOrderProducts()[i];%>
													<tr>
														<td>
						    								<a href='<%="SelectProd.action?prodId="+op.getProductId()%>'  class="text-link"><%=op.getName()%>
																<kk:prodOptions options="<%=op.getOpts()%>"/>																				
															</a>
															<div class="item-quantity"><kk:msg  key="common.quantity"/>: <%=op.getQuantity()%></div>
														</td>
														<%if (kkEng.displayPriceWithTax()) {%>
															<td  class="total-price right"><%=kkEng.formatPrice(op.getFinalPriceIncTax())%></td>
														<%} else {%>
															<td  class="total-price right"><%=kkEng.formatPrice(op.getFinalPriceExTax())%></td>
														<%}%>	
													</tr>
												<%}%>
											<%}%>
											<%if (order.getOrderTotals() != null && order.getOrderTotals().length > 0){ %>
												<% for (int i = 0; i < order.getOrderTotals().length; i++){ %>
													<% com.konakart.appif.OrderTotalIf ot = order.getOrderTotals()[i];%>
													<%String rowClass = "costs-and-promotions";%>
													<%if (ot.getClassName().equals("ot_total")){ %>
														<%rowClass = "shopping-cart-total";%>
													<% } %>										
													<tr class="<%=rowClass%>">															
														<%if (ot.getClassName().equals("ot_reward_points")){%>
														    <td class="cost-overview"><%=ot.getTitle()%></td>	
															<td class="cost-overview-amounts right"><%=ot.getValue()%></td>
														<%}else if (ot.getClassName().equals("ot_free_product")) {%>
															<td class="cost-overview"><%=ot.getTitle()%></td>
															<td class="cost-overview-amounts right"><%=ot.getText()%></td>
														<%}else if (ot.getClassName().equals("ot_total")) {%>
															<td><%=ot.getTitle()%></td>
															<td class="right"><%=kkEng.formatPrice(ot.getValue())%></td>
														<%}else if (kkEng.isDiscountModule(ot.getClassName())) {%>	
														    <td class="cost-overview"><span class="discount"><%=ot.getTitle()%></span></td>
															<td class="cost-overview-amounts right"><span class="discount">-<%=kkEng.formatPrice(ot.getValue())%></span></td>
														<%}else{%>
														    <td class="cost-overview"><%=ot.getTitle()%></td>	
															<td class="cost-overview-amounts right"><%=kkEng.formatPrice(ot.getValue())%></td>
														<%}%>		    																		
													</tr>
												<%}%>
											<%}%>
				    					</tbody>	    				
		    						</table>
	    						<% } %>
							</div>
						</div>			    				
						<div id="confirm-order-button-container">	
							<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners">
								<span><kk:msg  key="common.confirmorder"/></span>
							</a>
						</div>
					</form>			    	
	    		</div>

  	    	<div id="error-dialog" title="<kk:msg  key="one.page.checkout.problem.title"/>" class="content-area rounded-corners">
	    		<div>
					<div class="form-section">
						<div class="form-section-title no-margin">
							<h3><kk:msg  key="one.page.checkout.problem"/></h3>									
						</div>
						<a onclick='closeErrorDialog();' class="button small-rounded-corners">
							<span ><kk:msg  key="common.close"/></span>
						</a>															
					</div>
		    	</div>
		    </div>
    		
 	    	<div id="addr-dialog" title="<kk:msg  key="header.address.book"/>" class="content-area rounded-corners">
	    		<div>
					<div class="form-section">
						<div class="form-section-title no-margin">
							<h3><kk:msg  key="address.book.dialog.select"/></h3>									
						</div>
						<%if (cust.getAddresses() != null && cust.getAddresses().length > 0){ %>
							<% for (int i = 0; i < cust.getAddresses().length; i++){ %>
								<% com.konakart.appif.AddressIf addr = cust.getAddresses()[i];%>						
								<div class="select-addr-section <%=(i%2==0)?"even":"odd"%>">
									<div class="select-addr">
										<%=kkEng.removeCData(addr.getFormattedAddress())%>
									</div>
									<div class="select-addr-buttons">
										<a onclick='<%="selectAddr("+addr.getId()+");"%>' class="button small-rounded-corners">
											<span ><kk:msg  key="common.select"/></span>
										</a>									
									</div>
								</div>
							<%}%>
						<%}%>
					</div>
		    	</div>
		    </div>





