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

   		<h1 id="page-title"><kk:msg  key="edit.wishlist.body.whatsinwishlist"/></h1>
    		<div id="edit-wish-list" class="wide">
	    		<div id="checkout-area" class="content-area rounded-corners">
				<s:set scope="request" var="itemList" value="itemList"/> 
				<% java.util.ArrayList<com.konakart.al.WishListUIItem> itemList = (java.util.ArrayList<com.konakart.al.WishListUIItem>)request.getAttribute("itemList");%>
				<%if (itemList == null || itemList.size() == 0){ %>
					<p><kk:msg  key="edit.wishlist.body.emptywishlist"/></p>
				<% } else { %>				    		
		    		<form action="EditWishListSubmit.action" id="form1" method="post" class="form-section">
		    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<s:set scope="request" var="id" value="id"/> 
						<% int id = ((Integer)request.getAttribute("id")).intValue();%>
						<input type="hidden" name="id" value="<%=id%>"/>
	    				<table>
	    					<thead>
	    						<tr>
	    							<td id= "image-col-wishlist" class="narrow-col"><kk:msg  key="common.item"/></td>
	    							<td id= "item-col" class="item-col"></td>
	    							<td class="priority-col-wishlist"><kk:msg  key="edit.wishlist.body.priority"/></td>
	    							<td id= "price-col" lass="narrow-col right"><kk:msg  key="edit.wishlist.body.price"/></td>
	    							<td id= "add-to-cart-col" class="wide-col center"></td>
	    							<td id="remove-col" class="narrow-col center"></td>
	    						</tr>
	    					</thead>
	    					<tbody>
	    						<% int k = 0;%>
								<%for (java.util.Iterator<com.konakart.al.WishListUIItem> iterator = itemList.iterator(); iterator.hasNext();){%>
									<%com.konakart.al.WishListUIItem item = iterator.next();%>
					               <input type="hidden" name="itemList[<%=k%>].wishListItemId" value="<%=item.getWishListItemId()%>"/>
					               <input type="hidden" name="itemList[<%=k%>].quantityDesired" value="<%=item.getQuantityDesired()%>"/>
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
		    							<td class="right">
											<%if (kkEng.displayPriceWithTax()){%>
												<%=kkEng.formatPrice(item.getTotalPriceIncTax() )%>
											<%}else{%>
												<%=kkEng.formatPrice(item.getTotalPriceExTax())%>
											<%}%>		    																		
		    							</td>	    							
		    							<td class="center">
											<%if (kkEng.getQuotaMgr().canAddToBasket(item.getProdId(), null) > 0){ %>
												<a href='<%="EditWishListSubmit.action?action=a&wid="+id+"&id="+item.getWishListItemId()%>' class="button-small small-rounded-corners add-to-cart-text"><kk:msg  key="common.add.to.cart"/></a>
												<a href='<%="EditWishListSubmit.action?action=a&wid="+id+"&id="+item.getWishListItemId()%>' class="fa fa-shopping-cart button-small small-rounded-corners add-to-cart-icon" ></a>	
											<% } %>
		    							</td>
		    							<td class="center">
		    								<a class="remove fa fa-times-circle" href='<%="EditWishListSubmit.action?action=r&wid="+id+"&id="+item.getWishListItemId()%>' title='<%=kkEng.getMsg("common.remove.item")%>'></a>
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
							<a href="Welcome.action" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="edit.cart.body.continue.shopping"/></span></a>						
						</div>
					</form>		
				<% } %>	    	
	    		</div>
	    	</div>	
