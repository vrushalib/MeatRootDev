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
<%@page import="java.util.Iterator"%>
<%@page import="com.konakart.al.ProdOptionContainer"%>
<%@page import="com.konakart.al.ProdOption"%>
<%@page import="com.konakart.app.DataDescConstants"%>
<%@page import="com.konakart.app.Option"%>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.ProductMgr prodMgr = kkEng.getProductMgr();%>
<% com.konakart.al.ReviewMgr revMgr = kkEng.getReviewMgr();%>
<% com.konakart.appif.ProductIf prod = prodMgr.getSelectedProduct();%>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>
<%int rating = (prod.getRating()==null)?0:prod.getRating().setScale(0, java.math.BigDecimal.ROUND_HALF_UP).intValue();  %>
<%String ratingDecStr = (prod.getRating()==null)?"0":(prod.getRating().setScale(1, java.math.BigDecimal.ROUND_HALF_UP)).toPlainString();  %>
<%boolean isSmall = (kkEng.getCustomTemp1().equalsIgnoreCase("small"))?true:false;%>
<%String addToCartOrWishList=null; %>
			    <%if (isSmall){%>
					<form action="AddToCartOrWishListFromPost.action" id="AddToCartFormSmall" method="post">
					<% addToCartOrWishList = "addtoCartOrWishListFunc('AddToCartFormSmall')"; %>
	        	<%} else {%>
					<form action="AddToCartOrWishListFromPost.action" id="AddToCartForm" method="post">
					<% addToCartOrWishList = "addtoCartOrWishListFunc('AddToCartForm')"; %>
	        	<%}%>
						<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<input type="hidden" name="random" id="random" value="<%=System.currentTimeMillis()%>" />
						<input type="hidden" name="addToWishList" id="addToWishList" class="addToWishList" value="" />
						<input type="hidden" name="prodId" value="<%=prod.getId()%>" />
						<input type="hidden" name="wishListId" id="wishListId" class="wishListId" value="-1" />
						<div id="product-price">
							<%java.math.BigDecimal saving = null;%>
							<%if (kkEng.displayPriceWithTax()){%>
							    <%if (prod.getSpecialPriceIncTax() != null){%>
					                <%saving = prod.getPriceIncTax().subtract(prod.getSpecialPriceIncTax());%>
					                <span class="product-price-old"><%=kkEng.formatPrice(prod.getPriceIncTax())%></span>
					                <span class="product-price-current"><%=kkEng.formatPrice(prod.getSpecialPriceIncTax())%></span>
					        	<%} else {%>
					                <span class="product-price-current"><%=kkEng.formatPrice(prod.getPriceIncTax())%></span>
					        	<%}%>
					        <%} else {%>
					            <%if (prod.getSpecialPriceExTax() != null) {%>
					                <%saving = prod.getPriceExTax().subtract(prod.getSpecialPriceExTax());%>
					                <span class="product-price-old"><%=kkEng.formatPrice(prod.getPriceExTax())%></span>
					                <span class="product-price-current"><%=kkEng.formatPrice(prod.getSpecialPriceExTax())%></span>
					        	<%} else {%>
					                <span class="product-price-current"><%=kkEng.formatPrice(prod.getPriceExTax())%></span>
					            <%}%>
							<%}%>
						</div>
						<div class="rating-big">
				            <%if (prod.getNumberReviews()>0) {%>
					            <a href='<%="SelectProd.action?prodId=" + prod.getId()+"&showRevs=t"%>'>
							        <%for (int i = 0; i < rating; i++){%>
							            <span class="star-big full"></span>
							        <%}%>
							        <%for (int i = rating; i < 5; i++) {%>
							            <span class="star-big empty"></span>
							        <%}%>
									<span id="star-reviews-link">(<%=prod.getNumberReviews()%>&nbsp;<kk:msg  key="common.reviews"/>)</span>
								</a>
				            <%}else{%>
						        <%for (int i = 0; i < rating; i++){%>
						            <span class="star-big full"></span>
						        <%}%>
						        <%for (int i = rating; i < 5; i++) {%>
						            <span class="star-big empty"></span>
						        <%}%>
								<span id="star-reviews-link">(<%=prod.getNumberReviews()%>&nbsp;<kk:msg  key="common.reviews"/>)</span>
				            <%}%>
	   					</div>	
	   					<%int qtyWarn = kkEng.getStockWarnLevel();%>
	   					<%if (prod.getQuantity() > qtyWarn){%>
				            <div id="left-in-stock"><kk:msg  key="product.details.body.in.stock"/></div>  
				        <%} else if (prod.getQuantity() <= qtyWarn && prod.getQuantity() > 0){%>
				            <div id="left-in-stock"><kk:msg  key="product.details.body.limited.stock" arg0="<%=Integer.toString(prod.getQuantity())%>"/></div>  
				        <%} else{%>
				            <div id="left-in-stock"><kk:msg  key="product.details.body.out.of.stock"/></div>  
				        <%}%>
	   					<div class="labels">
	   					    <%if (prod.getType() == com.konakart.bl.ProductMgr.FREE_SHIPPING || prod.getType() == com.konakart.bl.ProductMgr.FREE_SHIPPING_BUNDLE_PRODUCT_TYPE){%>
	   							<div class="label free-shipping"><kk:msg  key="product.tile.free.shipping"/></div>
	   						<%}%>
	   						<%if (saving != null){%>
	   							<div class="label save"><kk:msg  key="common.save"/>&nbsp;<%=kkEng.formatPrice(saving)%></div>
	   						<%}%>
	   					</div>		
	   					<div id="product-options">
	   						<%if (prod.getOpts() != null && prod.getOpts().length > 0){ %>
								<% int i=0; %>	  
								<table class="product-option">   
								<%for (Iterator<ProdOptionContainer> iterator =  prodMgr.getSelectedProductOptions().iterator(); iterator.hasNext();) { %>
									<% ProdOptionContainer optContainer =  iterator.next();%>	
									<input type="hidden" name="optionId[<%=i%>]" value="<%=optContainer.getId()%>" />
									<input type="hidden" name="type[<%=i%>]" value="<%=optContainer.getType()%>" />
									<tr>
									<%if (Integer.parseInt(optContainer.getType()) == Option.TYPE_SIMPLE){%>
										<td class="opt-name"><%=optContainer.getName() %></td>
										<td class="opt-value">
											<select name="valueId[<%=i%>]">
												<%for (Iterator<ProdOption> iterator1 =  optContainer.getOptValues().iterator(); iterator1.hasNext();) {  %>																	
													<% ProdOption option =  iterator1.next();%>	
													<%if (kkEng.displayPriceWithTax()){%>
														 <option value="<%=option.getId()%>"><%=option.getFormattedValueIncTax() %></option>
													<%}else{%>
														<option value="<%=option.getId()%>"><%=option.getFormattedValueExTax() %></option>														
													<%}%>
												<%}%>
											</select> 
										</td>
									<%}else if (Integer.parseInt(optContainer.getType()) == Option.TYPE_VARIABLE_QUANTITY) {%>
										<td class="opt-name"><%=optContainer.getName()+" "%>
											<%if (kkEng.displayPriceWithTax()){%>
												<%=optContainer.getOptValues().get(0).getFormattedValueIncTax()%>
											<%}else{%>
												<%=optContainer.getOptValues().get(0).getFormattedValueExTax()%>
											<%}%>
										</td> 
										<td class="opt-value">
											<input type="text" class="numeric" name="quantity[<%=i%>]" digits="true" maxlength="10"/>
											<br/><span class="validation-msg"></span>
											<input type="hidden"  name="valueId[<%=i%>]" value="<%= Integer.toString(optContainer.getOptValues().get(0).getId()) %>" />	
										</td>
									<%}else if (Integer.parseInt(optContainer.getType()) == Option.TYPE_CUSTOMER_PRICE) {%>
										<td class="opt-name"><%=optContainer.getName()+" "%>
											<%if (kkEng.displayPriceWithTax()){%>
												<%=optContainer.getOptValues().get(0).getFormattedValueIncTax()%>
											<%}else{%>
												<%=optContainer.getOptValues().get(0).getFormattedValueExTax()%>
											<%}%>
										</td> 
										<td class="opt-value">
											<input type="text" class="numeric" name="custPrice[<%=i%>]" number="true" maxlength="10"/>
											<br/><span class="validation-msg"></span>
											<input type="hidden"  name="valueId[<%=i%>]" value="<%= Integer.toString(optContainer.getOptValues().get(0).getId()) %>" />	
										</td>
									<%}else if (Integer.parseInt(optContainer.getType()) == Option.TYPE_CUSTOMER_TEXT) {%>
										<td class="opt-name"><%=optContainer.getName()+" "%>
											<%if (kkEng.displayPriceWithTax()){%>
												<%=optContainer.getOptValues().get(0).getFormattedValueIncTax()%>
											<%}else{%>
												<%=optContainer.getOptValues().get(0).getFormattedValueExTax()%>
											<%}%>
										</td> 
										<td class="opt-value">
											<input type="text" class="cust-text" name="custText[<%=i%>]" maxlength="512"/>
											<br/><span class="validation-msg"></span>
											<input type="hidden"  name="valueId[<%=i%>]" value="<%= Integer.toString(optContainer.getOptValues().get(0).getId()) %>" />	
										</td>
									<%}%> 
									</tr>
									<% i++; %>			            				            			            			   			
								<%}%>
								</table> 
								<input type="hidden" name="numOptions" value="<%=new Integer(i).toString()%>" />
						 	<%}%>
	   					</div>	
	   					<div class="product-buttons">
	   						<select name="prodQuantity" id="prodQuantityId" class="add-to-cart-qty">
								<%for (int i=1; i<31; i++) {  %>																	
									<option value="<%=i%>"><%=i%></option>
								<%}%>
							</select> 
	   					    <%if (kkEng.getQuotaMgr().canAddToBasket(prod, null) > 0){%>
		    					<a onmouseover="resetAddToWishList()" onclick="<%=addToCartOrWishList%>" class="add-to-cart-button-big button small-rounded-corners"><kk:msg  key="common.add.to.cart"/></a>
		    				<%}%>
	   						<div class="add-to-wishlist-container">
	   							<%if (kkEng.isWishListEnabled()) { %>
		   							<div class="add-to-wishlist-link">
			   							<img class="plus-button" src="<%=kkEng.getImageBase()%>/plus-button.png"/>
			   							<a onmouseover="setAddToWishList()" onclick="<%=addToCartOrWishList%>" class="add-to-wishlist-prod-details"><kk:msg  key="common.add.to.wishlist"/></a>
									</div>
								<%}%>
								<%if (kkEng.getConfigAsBoolean("ENABLE_GIFT_REGISTRY",false) && cust.getWishLists() != null && cust.getWishLists().length > 0){ %>
									<% for (int i = 0; i < cust.getWishLists().length; i++){ %>
										<% com.konakart.appif.WishListIf wishList = cust.getWishLists()[i];%>									
										<%if (wishList != null && wishList.getListType()!= com.konakart.al.WishListMgr.WISH_LIST_TYPE ) {%>
											<div class="add-to-wishlist-link">
												<img class="plus-button" src="<%=kkEng.getImageBase()%>/plus-button.png"></img>
												<a onmouseover='<%="setWishListId("+ new Integer(wishList.getId()).toString()+")"%>' onclick="<%=addToCartOrWishList%>" class="add-to-wishlist-prod-details"><kk:msg  key="product.details.body.add.product"/>&nbsp;<%=wishList.getName()%></a>
											</div>
										<%}%>
									<%}%>
								<%}%>
	   						</div>
	    				</div>			
   						<div id="notify-me-container">
	   						<%if (prodMgr.isCurrentProductANotification()) { %>
								<a href="ResetNotification.action">
									<kk:msg  key="common.remove"/> <b><%=prod.getName()%></b> <kk:msg  key="product.details.body.dontnotifyme"/>
								</a>
							<% } else { %>
								<a href="SetNotification.action">
									<kk:msg  key="product.details.body.notifyme"/> <b><%=prod.getName()%></b>
								</a>
							<% } %>				
     					</div>  					
					</form>