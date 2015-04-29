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
<% com.konakart.appif.CustomerIf currentCustomer = customerMgr.getCurrentCustomer();%>

<script>
$(function() {	
	if ($("#form1").length) {
		$("#form1").validate(validationRules);
	    $("#form1 input[name='prodQty']").each(function() {
	        $(this).rules("add", { required: true, digits: true, maxlength: 7 });
	     }); 
	}

	var qtyMap = {};
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
		  var buttonId = 'w-'+(id).split('-')[1];
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
		  var itemId = (id).split('-')[1];
		  if (itemId == null) {
			  document.getElementById('form1').submit();
		  } else {
			  var inputId = 'q-'+itemId;
			  var qty =  $('#'+inputId).val();
			  var wlId =  $('#wish-list-id').val();
			  qtyMap[inputId] = qty;
			  elem.hide();
			  redirect(getURL("EditWishListSubmit.action", new Array("action","q","wid",wlId,"id",itemId,"qty",qty)));
		  }
	});
});



</script>

	   		<h1 id="page-title">
				<s:set scope="request" var="listName" value="listName"/> 
				<% String listName = (String)request.getAttribute("listName");%>
				<%=listName%>
	   		</h1>
    		<div id="edit-wish-list" class="wide">
	    		<div id="checkout-area" class="content-area rounded-corners">
				<s:set scope="request" var="itemList" value="itemList"/> 
				<% java.util.ArrayList<com.konakart.al.WishListUIItem> itemList = (java.util.ArrayList<com.konakart.al.WishListUIItem>)request.getAttribute("itemList");%>
				<%if (itemList == null || itemList.size() == 0){ %>
					<p><kk:msg  key="edit.gift.registry.item.body.empty"/></p>
				<% } else { %>				    		
		    		<form action="EditWishListSubmit.action" id="form1" method="post" class="form-section">
		    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<s:set scope="request" var="id" value="id"/> 
						<% int id = ((Integer)request.getAttribute("id")).intValue();%>
						<input type="hidden" id="wish-list-id" name="id" value="<%=id%>"/>
	    				<table>
	    					<thead>
	    						<tr>
	    							<td class="image-col-edit"><kk:msg  key="common.item"/></td>
	    							<td class="item-col"></td>
	    							<td class="priority-drop-col"><kk:msg  key="edit.wishlist.body.priority"/></td>
	    							<td class="q-desired-col"><kk:msg  key="edit.wishlist.body.q.desired"/></td>
	    						
	    							<td class="price-col right"><kk:msg  key="edit.wishlist.body.price"/></td>
	    							<td class="delete-col center"></td>
	    						</tr>
	    					</thead>
	    					<tbody>
	    						<% int k = 0;%>
								<%for (java.util.Iterator<com.konakart.al.WishListUIItem> iterator = itemList.iterator(); iterator.hasNext();){%>
									<%com.konakart.al.WishListUIItem item = iterator.next();%>
					               <input type="hidden" name="itemList[<%=k%>].wishListItemId" value="<%=item.getWishListItemId()%>"/>
		    						<tr>
		    							<td class="product-image-td">
		    							    <img class="product-image" src="<%=item.getProdImage()%>" border="0" alt="<%=item.getProdName()%>" title=" <%=item.getProdName()%> ">
		    							</td>
		    							<td>		    								
		    								<a href='<%="SelectProd.action?prodId="+item.getProdId()%>'  class="text-link"><%=item.getProdName()%>
		    									<%if (item.getOptNameArray() != null && item.getOptNameArray().length > 0){ %>
													<% for (int l = 0; l < item.getOptNameArray().length; l++){ %>
														<% String optName = item.getOptNameArray()[l];%>
														<br><span class="shopping-cart-item-option"> - <%=optName%></span>
													<% } %>																								
												<% } %>
											</a>
		    							</td>
		    							<td>
											<select  name="itemList[<%=k%>].priority" onchange="javascript:document.getElementById('form1').submit();">
												<option value="5" <%=(item.getPriority()==5)?"selected=\"selected\"":""%>><kk:msg  key="common.highest"/></option>
												<option value="4" <%=(item.getPriority()==4)?"selected=\"selected\"":""%>><kk:msg  key="common.high"/></option>
												<option value="3" <%=(item.getPriority()==3)?"selected=\"selected\"":""%>><kk:msg  key="common.medium"/></option>
												<option value="2" <%=(item.getPriority()==2)?"selected=\"selected\"":""%>><kk:msg  key="common.low"/></option>
												<option value="1" <%=(item.getPriority()==1)?"selected=\"selected\"":""%>><kk:msg  key="common.lowest"/></option>
											</select>
		    							</td>
		    							<td>
		    								<span class="qty-desired"><span class="qty-desired-label"><kk:msg  key="show.giftregistry.items.body.desired"/>:</span> <input type="text" class="qty-input" name="prodQty" id="q-<%=item.getWishListItemId()%>" value="<%=item.getQuantityDesired()%>">
		    								<a id='<%="w-"+item.getWishListItemId()%>' class="update-button small-rounded-corners"><kk:msg  key="common.update"/></a>
		    								<span class="validation-msg"></span></span>
		    								<span class="qty-received"><span class="qty-received-label"><kk:msg  key="show.giftregistry.items.body.received"/>:</span> <%=item.getQuantityReceived() %></span>
		    							</td>
		    						
		    							<td class="right">
											<%if (kkEng.displayPriceWithTax()){%>
												<%=kkEng.formatPrice(item.getTotalPriceIncTax() )%>
											<%}else{%>
												<%=kkEng.formatPrice(item.getTotalPriceExTax())%>
											<%}%>		    																		
		    							</td>	    							
		    							<td class="center">
		    								<a class="remove fa fa-times-circle" href='<%="EditWishListSubmit.action?action=r&wid="+id+"&id="+item.getWishListItemId()%>'  title='<%=kkEng.getMsg("common.remove.item")%>'></a>
		    							</td>
		    					    </tr>
		    					    <% k++; %>	
								<%}%>
	    						<tr id="costs-and-promotions">
	    							<td id="promotion-codes" colspan="2">
	    							</td>	    							
	    							<td id="cost-overview" colspan="3">
										<table>    									
											<tr>
												<td class="cost-overview-labels">
													<kk:msg  key="common.total"/>:
												</td>
												<td class="cost-overview-amounts">
													<%if (kkEng.displayPriceWithTax()){%>
														<s:set scope="request" var="finalPriceIncTax" value="finalPriceIncTax"/> 
														<% java.math.BigDecimal finalPriceIncTax = (java.math.BigDecimal)request.getAttribute("finalPriceIncTax");%>
														<%if (finalPriceIncTax != null){ %>
															<%=kkEng.formatPrice(finalPriceIncTax)%>
														<%}%>
													<%}else{%>
														<s:set scope="request" var="finalPriceExTax" value="finalPriceExTax"/> 
														<% java.math.BigDecimal finalPriceExTax = (java.math.BigDecimal)request.getAttribute("finalPriceExTax");%>
														<%if (finalPriceExTax != null){ %>
															<%=kkEng.formatPrice(finalPriceExTax)%>
														<%}%>
													<%}%>		    																		
												</td>
											</tr>		    																		
    									</table>										
	    							</td>
									<td></td>
	    						</tr>	    						
	    					</tbody>	    				
	    				</table>
						<div >
							<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
						</div>
					</form>		
				<% } %>	    	
	    		</div>
	    	</div>	
