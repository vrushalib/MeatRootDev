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

<%@page import="com.konakart.appif.BasketIf"%>
<%@page import="com.konakart.appif.OrderTotalIf"%>
<%@page import="java.math.BigDecimal"%>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf currentCustomer = customerMgr.getCurrentCustomer();%>
<% com.konakart.al.ProductMgr prodMgr = kkEng.getProductMgr();%>
<% com.konakart.al.OrderMgr orderMgr = kkEng.getOrderMgr();%>
<% com.konakart.al.RewardPointMgr rewardPointMgr = kkEng.getRewardPointMgr();%>

<script type="text/javascript">

	function setGoToCheckout() {
		document.getElementById('goToCheckout').value="true";
		//alert(document.form1.goToCheckout.value);
	}

	function resetGoToCheckout() {
		document.getElementById('goToCheckout').value="false";
		//alert(document.form1.goToCheckout.value);
	}
	
$(function() {	
 	    if ($("#form1").length) {
 			$("#form1").validate(validationRules);
		    $("#form1 input[name='prodQty']").each(function() {
		        $(this).rules("add", { required: true, digits: true, maxlength: 7 });
		     }); 
		}
	
		var qtyMap = {};
		var couponCode=null;
		var giftCertCode=null;
		var rewardPoints=null;
		
		$('#couponCode').keydown(function() {
			  var elem = $(this);
			  var id = this.id;
			  if (couponCode==null) {
				  couponCode = elem.val();
			  }
		});
		$('#couponCode').keyup(function() {
			  var elem = $(this);
			  var val = elem.valid();
			  if (val==1 || elem.val()=="") {
				  if (elem.val() != couponCode) {
					  $('#couponCodeUpdate').show();
				  }else{
					  $('#couponCodeUpdate').hide();
				  }
			  } else {
				  $('#couponCodeUpdate').hide();
			  }
		});
		
		$('#giftCertCode').keydown(function() {
			  var elem = $(this);
			  var id = this.id;
			  if (giftCertCode==null) {
				  giftCertCode = elem.val();
			  }
		});
		$('#giftCertCode').keyup(function() {
			  var elem = $(this);
			  var val = elem.valid();
			  if (val==1 || elem.val()=="") {
				  if (elem.val() != giftCertCode) {
					  $('#giftCertCodeUpdate').show();
				  }else{
					  $('#giftCertCodeUpdate').hide();
				  }
			  } else {
				  $('#giftCertCodeUpdate').hide();
			  }
		});

		$('#rewardPoints').keydown(function() {
			  var elem = $(this);
			  var id = this.id;
			  if (rewardPoints==null) {
				  rewardPoints = elem.val();
			  }
		});
		$('#rewardPoints').keyup(function() {
			  var elem = $(this);
			  var val = elem.valid();
			  if (val==1 || elem.val()=="") {
				  if (elem.val() != rewardPoints) {
					  $('#rewardPointsUpdate').show();
				  }else{
					  $('#rewardPointsUpdate').hide();
				  }
			  } else {
				  $('#rewardPointsUpdate').hide();
			  }
		});
		
		$('.qty-input').keydown(function() {
			  var elem = $(this);
			  var id = this.id;
			  if (qtyMap[id]==null) {
				  qtyMap[id] = elem.val();
			  }
		});
		$('.qty-input').keyup(function() {
			  var elem = $(this);
			  var id = this.id;
			  var oldQty = qtyMap[id];
			  var buttonId = 'b-'+(id).split('-')[1];
			  var val = elem.valid();
			  if (val==1) {
				  if (elem.val() != oldQty) {
					  $('#'+buttonId).show();
				  }else{
					  $('#'+buttonId).hide();
				  }
			  } else {
				  $('#'+buttonId).hide();
			  }
		});
		$('.update-button').click(function() {
			  var elem = $(this);
			  var id = this.id;
			  var basketId = (id).split('-')[1];
			  if (basketId == null) {
				  document.getElementById('form1').submit();
			  } else {
				  var inputId = 'q-'+basketId;
				  var qty =  $('#'+inputId).val();
				  qtyMap[inputId] = qty;
				  elem.hide();
				  redirect(getURL("EditCartSubmit.action", new Array("action","q","id",basketId,"qty",qty)));
			  }
		});
	});
</script>
   		<h1 id="page-title"><kk:msg  key="edit.cart.body.editcart"/></h1>
 	    		<div id="checkout-area" class="content-area rounded-corners">
	    		<%if (currentCustomer.getBasketItems() == null || currentCustomer.getBasketItems().length == 0){ %>
					<p><kk:msg  key="edit.cart.body.emptycart"/></p>
				<% } else { %>	
	    			<s:if test="hasActionErrors()">
					   <div class="messageStackError">  
					        <s:iterator value="actionErrors">  
					            <s:property escape="false"/>
					        </s:iterator>  
		    			</div>  
					</s:if>		    		    		
							    		
		    		<form action="EditCartSubmit.action" id="form1" method="post" class="form-section">
		    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<input type="hidden" name="goToCheckout" id="goToCheckout" value="" />
						<% boolean outOfStock=false; %>	
	    				<table>
	    					<thead>
	    						<tr>
	    							<td class="narrow-col"><kk:msg  key="edit.cart.body.item"/></td>
	    							<td class="wide-col"></td>
	    							
	    							<td class="narrow-col right"><kk:msg  key="edit.cart.body.price"/></td>
	    							<td class="narrow-col right"><kk:msg  key="edit.cart.body.total"/></td>
	    							<td class="narrow-col center"></td>
	    						</tr>
	    					</thead>
	    					<tbody>
								<% for (int k = 0; k < currentCustomer.getBasketItems().length; k++){ %>
									<% BasketIf item = currentCustomer.getBasketItems()[k];%>
		    						<tr>
		    							<td>
		    								<%if ((item.getQuantity() > item.getQuantityInStock()) && prodMgr.isStockCheck()) { %>
													<div class="items-left red"><kk:msg  key="product.tile.out.of.stock"/></div>
													<% outOfStock=true; %>
											<% } %>
		    								<img class="product-image" src="<%=kkEng.getProdImage(item.getProduct(), com.konakart.al.KKAppEng.IMAGE_TINY)%>" border="0" alt="<%=item.getProduct().getName()%>" title="<%=item.getProduct().getName()%>"/>
		    							</td>
		    							<td>		    								
		    								<a href='<%="SelectProd.action?prodId="+item.getProduct().getId()%>'  class="text-link"><%=item.getProduct().getName()%>
		    									<kk:prodOptions options="<%=item.getOpts()%>"/>
											</a>
											<input type="text" class="qty-input" name="prodQty" id="q-<%=item.getId()%>" value="<%=item.getQuantity()%>">
		    								<a id='<%="b-"+item.getId()%>' class="update-button small-rounded-corners"><kk:msg  key="common.update"/></a>
		    								<span class="validation-msg"></span>
		    							</td>
		    						
				    					<%if (item.getFinalPriceIncTax() != null && kkEng.displayPriceWithTax()){ %>				    					
				    						<td class="right">
				    							<%if (item.getQuantity()>0){ %>
				    								<%=kkEng.formatPrice(item.getFinalPriceIncTax().divide(new BigDecimal(item.getQuantity()), BigDecimal.ROUND_HALF_UP))%>
				    							<% } %>
				    						</td>
											<td class="total-price right"><%=kkEng.formatPrice(item.getFinalPriceIncTax())%></td>	
										<% } %>							
										<%if (item.getFinalPriceExTax() != null && !kkEng.displayPriceWithTax()){ %>
				    						<td class="right">
				    							<%if (item.getQuantity()>0){ %>
				    								<%=kkEng.formatPrice(item.getFinalPriceExTax().divide(new BigDecimal(item.getQuantity()), BigDecimal.ROUND_HALF_UP))%>
				    							<% } %>
				    						</td>
											<td class="total-price right"><%=kkEng.formatPrice(item.getFinalPriceExTax())%></td>
										<% } %>		    							
		    							<td class="center"><a class="remove fa fa-times-circle" href='<%="EditCartSubmit.action?action=r&id="+item.getId()%>' title='<%=kkEng.getMsg("common.remove.item")%>'></a></td>
		    					    </tr>
								<%}%>
	    						<tr id="costs-and-promotions">
	    							<td id="promotion-codes" colspan="2">
		    							<div id="promotion-codes-container">
									    	<%if (kkEng.getConfigAsBoolean("DISPLAY_COUPON_ENTRY",false)) { %>
									    		<div class="promotion-codes-field">				
													<label><kk:msg  key="checkout.common.couponcode"/></label>
													<input type="text" name="couponCode" id="couponCode" value="<s:property value="couponCode" />"/>
													<a id="couponCodeUpdate" class="update-button small-rounded-corners" onmouseover="resetGoToCheckout()"><kk:msg  key="common.update"/></a>
													<span class="validation-msg"></span>
												</div>
											<% } %>
											<%if (kkEng.getConfigAsBoolean("DISPLAY_GIFT_CERT_ENTRY",false)) { %>
												<div class="promotion-codes-field">				
													<label><kk:msg  key="checkout.common.giftcertcode"/></label>
													<input type="text" name="giftCertCode" id="giftCertCode" value="<s:property value="giftCertCode" />"/>
													<a id="giftCertCodeUpdate" class="update-button small-rounded-corners" onmouseover="resetGoToCheckout()"><kk:msg  key="common.update"/></a>
													<span class="validation-msg"></span>
												</div>
											<% } %>
											<%if (kkEng.getConfigAsBoolean("ENABLE_REWARD_POINTS",false)) { %>			
												<%int points = rewardPointMgr.pointsAvailable(); %>
												<%if  (points > 0) { %>
													<div class="promotion-codes-field">	
														<label><kk:msg  key="checkout.common.reward_points" arg0="<%=Integer.toString(points)%>"/></label>
														<input type="text" name="rewardPoints" id="rewardPoints" value="<s:property value="rewardPoints" />"/>
														<a id="rewardPointsUpdate" class="update-button small-rounded-corners" onmouseover="resetGoToCheckout()"><kk:msg  key="common.update"/></a>
														<span class="validation-msg"></span>
													</div>
												<% } %>
											<% } %>
										</div>
	    							</td>	    							
	    							<td id="cost-overview" colspan="3">
								    	<%if (orderMgr.getCheckoutOrder() != null){ %>
											<%if (orderMgr.getCheckoutOrder().getOrderTotals() != null && orderMgr.getCheckoutOrder().getOrderTotals().length > 0 ){ %>
												<table>    									
													<% for (int j = 0; j < orderMgr.getCheckoutOrder().getOrderTotals().length; j++){ %>
														<tr>
															<% OrderTotalIf ot = orderMgr.getCheckoutOrder().getOrderTotals()[j];%>
															<td class="cost-overview-labels">
																<% if (kkEng.isDiscountModule(ot.getClassName())) {%>																
																	<span class="discount"><%=ot.getTitle()%></span><br/>
																<%}else{%>																
																	<%=ot.getTitle()%><br/>
																<%}%>
															</td>
															<td class="cost-overview-amounts right">
																<%if (ot.getClassName().equals("ot_reward_points")){%>
																	<%=ot.getValue()%><br/>
																<%}else if (ot.getClassName().equals("ot_free_product")){%>
																	<%=ot.getText()%><br/>
																<%}else if (kkEng.isDiscountModule(ot.getClassName())) {%>														
																	<span class="discount">-<%=kkEng.formatPrice(ot.getValue())%></span><br/>
																<%}else{%>																
																	<%=kkEng.formatPrice(ot.getValue())%><br/>
																<%}%>
															</td>
														</tr>		    																		
													<% } %>
		    									</table>										
											<% } %>
										<% } else { %>
											<table>    									
												<tr>
													<td class="cost-overview-labels">
														<kk:msg  key="edit.cart.body.subtotal"/>:
													</td>
													<td class="cost-overview-amounts">
														<%=kkEng.getBasketMgr().getFormattedBasketTotal()%>
													</td>
												</tr>		    																		
	    									</table>										
										<% } %>
	    							</td>
									<td></td>
	    						</tr>	    						
	    					</tbody>	    				
	    				</table>
						<div >
							<a onmouseover="setGoToCheckout()" onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.checkout"/></span></a>						
						</div>
					</form>		
				<% } %>	    	
	    		</div>
