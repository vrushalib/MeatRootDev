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
<% String sortBy = revMgr.getDataDesc().getOrderBy();%>

<%if (!kkEng.isPortlet()){%>
	<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/kk.proddetails.js"></script>	
<% } %>

<%if (prodMgr.getSelectedProduct() == null){%>
	<kk:msg  key="product.details.body.product.not.found"/>
<% } else { %>
	<% com.konakart.al.WishListMgr wishListMgr = kkEng.getWishListMgr();%>
	<% com.konakart.appif.ProductIf prod = prodMgr.getSelectedProduct();%>
	<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
	<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>
	<%int rating = (prod.getRating()==null)?0:prod.getRating().setScale(0, java.math.BigDecimal.ROUND_HALF_UP).intValue();  %>
	<%String ratingDecStr = (prod.getRating()==null)?"0":(prod.getRating().setScale(1, java.math.BigDecimal.ROUND_HALF_UP)).toPlainString();  %>
	<%com.konakart.appif.ReviewSearchIf search = revMgr.getRevSearch();%>
	
   		<h1 id="page-title"><%=prod.getName()%></h1>
    	<div id="product-area" class="rounded-corners">
    		<div id="product-column-left">
	    		<div id="product-images-container" class="centered">
					<div id="gallery" class="gallery">
						<div class="gallery_output" id="gallery_output"></div>									
						<div id="gallery_nav" class="gallery_nav">
							<input id="gallery_nav_base" value="<%=kkEng.getImageBase()%>" type="hidden"/>								
							<input id="gallery_nav_dir" value="<%=prod.getImageDir()%>" type="hidden"/>								
							<input id="gallery_nav_uuid" value="<%=prod.getUuid()%>" type="hidden"/>								
							<input id="gallery_nav_extension" value="<%=kkEng.getProdImageExtension(prod)%>" type="hidden"/>								
						</div>																		
						<div class="clear"></div>
					</div>
				</div>
	    		<div id="product-content">
	    			<div id="product-content-tabs">
		    			<div id="product-description-tab" class="<%=revMgr.isShowTab()?"":"selected-product-content-tab"%> product-content-tab small-rounded-corners-top"><kk:msg  key="product.details.body.product.description"/></div>		
    			    	<div class="product-content-tab-spacer"></div>
		    			<div id="product-specifications-tab" class="product-content-tab small-rounded-corners-top"><kk:msg  key="product.details.body.specifications"/></div>
		    			<div class="product-content-tab-spacer"></div>
				    	<div id="product-reviews-tab" class="<%=revMgr.isShowTab()?"selected-product-content-tab":""%> product-content-tab small-rounded-corners-top"><kk:msg  key="product.details.body.reviews"/> (<%=prod.getNumberReviews()%>)</div>
		    			<div class="product-content-tab-filler"></div>
		    			<%revMgr.setShowTab(false);%>
		    		</div>
	    			<div id="product-description">
	    				<p style="clear:both"><%=prod.getDescription()%></p>		    			
	    			</div>
					<div id="product-specifications">	
						<%if (prod.getCustomAttrArray() != null && prod.getCustomAttrArray().length > 0){ %>
							<table>
		    					<thead>
		    						<th colspan="2"><kk:msg  key="product.details.body.product.specifications"/></th>
		    					</thead>
	    						<tbody>						
								<% for (int i = 0; i < prod.getCustomAttrArray().length; i++){ %>
									<% com.konakart.appif.ProdCustAttrIf attr =  prod.getCustomAttrArray()[i];%>	
									<%if (attr.getFacetNumber() == 0){ %>
										<tr class="<%=(i%2==0)?"even":"odd"%>"><td><kk:msg  key="<%=(attr.getMsgCatKey()==null)? attr.getName():attr.getMsgCatKey()%>"/>:</td><td><%=attr.getValue()%></td></tr>
									<%}%>
								<%}%>
	    						</tbody>
	    					</table>	
							<% } else { %>
								<p style="clear:both"><kk:msg  key="product.details.body.add.specifications"/></p>
							<%}%>												
	    			</div>    			
 	    			<div id="product-reviews">
 	    			<%if (prod.getNumberReviews() > 0){ %>
	    				<div id="average-customer-reviews">
		    				<div id="average-customer-reviews-meters">
		    					<% for (int i = 5; i > 0; i--){ %>
		    						<%int qty = revMgr.getRatingQuantity()[i-1]; %>
			    					<div class="average-customer-reviews-meter-container">
			    						<div class="average-customer-reviews-meter-label">
			    							<%if (qty > 0){ %> 
												<a href='<%="FilterRev.action?rating="+i%>'>
													<span class="<%=(search!=null&&search.getRating()==i)?"selected":"not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
												</a>
											<%}else{%>
												<span class="<%=(search!=null&&search.getRating()==i)?"selected":"not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
											<%}%>
			    						</div>
			    						<div class="average-customer-reviews-meter">
			    							<span id="<%=i%>star" style="width:<%=revMgr.getRatingPercentage()[i-1]%>%"></span>
			    						</div>
			    						<div class="average-customer-reviews-number">(<%=qty%>)</div>				    					
			    					</div>
								<%}%>    				
		    				</div>
		    				<div id="average-customer-reviews-stars">
		    					<div id="average-customer-reviews-stars-title"><kk:msg  key="show.reviews.body.average"/>:</div>
		    					<div class="rating-big">
							        <%for (int i = 0; i < rating; i++){%>
							            <span class="star-big full"></span>
							         <%}%>
							        <%for (int i = rating; i < 5; i++){%>
							            <span class="star-big empty"></span>
							        <%}%>
							        &nbsp;(<%=ratingDecStr%>)
		    					</div>
		    					<div class="write-review">
		    						<a href='<%="WriteReview.action?prodId="+prod.getId()%>' class="write-review-button button small-rounded-corners"><kk:msg  key="common.write.review"/></a>	
		    					</div>
		    				</div>
	    				</div>
	    				<div class="product-reviews-navigation">
	    					<div class="product-reviews-navigation-left">
		    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>				    					
			    				<span class="separator"></span>
								<span class="sort-by navigation-element navigation-dropdown">
									<form action="SortRev.action" method="post"><kk:msg  key="common.sort.by"/>:
										<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
										<select name="orderBy" onchange="submit()">					
											<option  value="<%=DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.most.recent"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.oldest"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.rating.desc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.rating.asc"/></option>
										</select>
										<input type="hidden" name="t" value="<%=revMgr.getRevTimestamp()%>"/>
									</form>	
								</span>
							</div>
							<div class="product-reviews-navigation-right">
								<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>"/>
			    				<span class="separator"></span>
	    						<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>
							</div>
						</div>
						<div id="product-reviews-area">
							<%int numRevs =  (revMgr.getCurrentReviews().length > revMgr.getPageSize()) ? revMgr.getPageSize() : revMgr.getCurrentReviews().length;%>
							<% for (int i = 0; i < numRevs; i++){ %>
								<% com.konakart.appif.ReviewIf rev = revMgr.getCurrentReviews()[i];%>
								<div class="product-review">	
									<div class="rating">
				    					<%for (int j = 0; j < rev.getRating(); j++){%>
								            <span class="star full"></span>
								        <%}%>
								        <%for (int j = rev.getRating(); j < 5; j++){%>
								            <span class="star empty"></span>
								        <%}%>					    						
			    					</div>
			    					<div class="product-review-details">
			    						<kk:msg  key="show.reviews.body.by"/> <span class="product-review-details-author"><%=rev.getCustomerName()%></span>
										<span class="product-review-details-date"><%=new java.text.SimpleDateFormat("EEEE d MMMM yyyy").format(rev.getDateAdded().getTime())%></span>
									</div>
									<p><%=rev.getReviewText()%></p>
								</div>
							<%}%>				
						</div>
	    				<div class="product-reviews-navigation">
	    					<div class="product-reviews-navigation-left">
		    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>				    					
			    				<span class="separator"></span>
								<span class="sort-by navigation-element navigation-dropdown">								 
									<form action="SortRev.action" method="post"><kk:msg  key="common.sort.by"/>:
										<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
										<select name="orderBy" onchange="submit()">					
											<option  value="<%=DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.most.recent"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.oldest"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.rating.desc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="show.reviews.body.sort.by.rating.asc"/></option>
										</select>
										<input type="hidden" name="t" value="<%=revMgr.getRevTimestamp()%>"/>
									</form>	
								</span>
							</div>
							<div class="product-reviews-navigation-right">
								<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>"/>
			    				<span class="separator"></span>
	    						<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>
							</div>
						</div>
					<% } else { %>
						<p style="clear:both"><a href='<%="WriteReview.action?prodId="+prod.getId()%>' class="text-link"><kk:msg  key="show.reviews.body.be.first"/></a></p>
					<% } %>		
	    			</div>	  
	    		</div>
    		</div>
			<div id="product-column-right">
				<div id="buying-details" class="rounded-corners-top">
					<form action="AddToCartOrWishListFromPost.action" id="AddToCartForm" method="post">
						<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<input type="hidden" name="random" id="random" value="<%=System.currentTimeMillis()%>" />
						<input type="hidden" name="addToWishList" id="addToWishList" value="" />
						<input type="hidden" name="prodId" value="<%=prod.getId()%>" />
						<input type="hidden" name="wishListId" id="wishListId" value="-1" />
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
								<%for (Iterator<ProdOptionContainer> iterator =  prodMgr.getSelectedProductOptions().iterator(); iterator.hasNext();) { %>
									<% ProdOptionContainer optContainer =  iterator.next();%>	
									<input type="hidden" name="optionId[<%=i%>]" value="<%=optContainer.getId()%>" />
									<input type="hidden" name="type[<%=i%>]" value="<%=optContainer.getType()%>" />
									<%if (Integer.parseInt(optContainer.getType()) == Option.TYPE_SIMPLE){%>
										<div class="product-option">										
											<span><%=optContainer.getName() %>:</span>
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
										</div>
									<%}else if (Integer.parseInt(optContainer.getType()) == Option.TYPE_VARIABLE_QUANTITY) {%>
										<div class="product-option">
											<label><%=optContainer.getName()+" "%>
												<%if (kkEng.displayPriceWithTax()){%>
													<%=optContainer.getOptValues().get(0).getFormattedValueIncTax()%>
												<%}else{%>
													<%=optContainer.getOptValues().get(0).getFormattedValueExTax()%>
												<%}%>
											</label> 
											<input type="text" class="numeric" name="quantity[<%=i%>]" digits="true" maxlength="10"/>
											<span class="validation-msg"></span>
											<input type="hidden"  name="valueId[<%=i%>]" value="<%= Integer.toString(optContainer.getOptValues().get(0).getId()) %>" />	
										</div>	
									<%}else if (Integer.parseInt(optContainer.getType()) == Option.TYPE_CUSTOMER_PRICE) {%>
										<div class="product-option">
											<label><%=optContainer.getName()+" "%>
												<%if (kkEng.displayPriceWithTax()){%>
													<%=optContainer.getOptValues().get(0).getFormattedValueIncTax()%>
												<%}else{%>
													<%=optContainer.getOptValues().get(0).getFormattedValueExTax()%>
												<%}%>
											</label> 
											<input type="text" class="numeric" name="custPrice[<%=i%>]" number="true" maxlength="10"/>
											<span class="validation-msg"></span>
											<input type="hidden"  name="valueId[<%=i%>]" value="<%= Integer.toString(optContainer.getOptValues().get(0).getId()) %>" />	
										</div>	
									<%}else if (Integer.parseInt(optContainer.getType()) == Option.TYPE_CUSTOMER_TEXT) {%>
										<div class="product-option">
											<label><%=optContainer.getName()+" "%>
												<%if (kkEng.displayPriceWithTax()){%>
													<%=optContainer.getOptValues().get(0).getFormattedValueIncTax()%>
												<%}else{%>
													<%=optContainer.getOptValues().get(0).getFormattedValueExTax()%>
												<%}%>
											</label> 
											<input type="text" class="cust-text" name="custText[<%=i%>]" maxlength="512"/>
											<span class="validation-msg"></span>
											<input type="hidden"  name="valueId[<%=i%>]" value="<%= Integer.toString(optContainer.getOptValues().get(0).getId()) %>" />	
										</div>	
									<%}%> 
									<% i++; %>			            				            			            			   			
								<%}%>
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
		    					<a onmouseover="resetAddToWishList()" onclick="addtoCartOrWishListFunc()" class="add-to-cart-button-big button small-rounded-corners"><kk:msg  key="common.add.to.cart"/></a>
		    				<%}%>
	   						<div class="add-to-wishlist-container">
	   							<%if (kkEng.isWishListEnabled()) { %>
		   							<div class="add-to-wishlist-link">
			   							<img class="plus-button" src="<%=kkEng.getImageBase()%>/plus-button.png"/>
			   							<a onmouseover="setAddToWishList()" onclick="addtoCartOrWishListFunc()" class="add-to-wishlist-prod-details"><kk:msg  key="common.add.to.wishlist"/></a>
									</div>
								<%}%>
								<%if (kkEng.getConfigAsBoolean("ENABLE_GIFT_REGISTRY",false) && cust.getWishLists() != null && cust.getWishLists().length > 0){ %>
									<% for (int i = 0; i < cust.getWishLists().length; i++){ %>
										<% com.konakart.appif.WishListIf wishList = cust.getWishLists()[i];%>									
										<%if (wishList != null && wishList.getListType()!= com.konakart.al.WishListMgr.WISH_LIST_TYPE ) {%>
											<div class="add-to-wishlist-link">
												<img class="plus-button" src="<%=kkEng.getImageBase()%>/plus-button.png"></img>
												<a onmouseover='<%="setWishListId("+ new Integer(wishList.getId()).toString()+")"%>' onclick="addtoCartOrWishListFunc()" class="add-to-wishlist-prod-details"><kk:msg  key="product.details.body.add.product"/>&nbsp;<%=wishList.getName()%></a>
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
				</div>
				<div id="share-this" class="rounded-corners-bottom">
    				<span id="share-this-label"><kk:msg  key="product.details.body.share.this"/>:</span>
    				<!-- AddThis Button BEGIN -->
					<div class="addthis_toolbox addthis_default_style ">
					<a class="addthis_button_preferred_1"></a>
					<a class="addthis_button_preferred_2"></a>
					<a class="addthis_button_preferred_3"></a>
					<a class="addthis_button_preferred_4"></a>
					<a class="addthis_button_compact"></a>
					<a class="addthis_counter addthis_bubble_style"></a>
					</div>
					<script type="text/javascript" src="//s7.addthis.com/js/300/addthis_widget.js#pubid=xa-50e1baaf57a7a27e"></script>
					<!-- AddThis Button END -->
    			</div>
  	   			<%if (prodMgr.getAllRelatedProducts() != null && prodMgr.getAllRelatedProducts().length > 0) { %>
	    			<div id="related-items" class="item-area-sidebar rounded-corners">
	    				<div class="item-area-header">
	    					<h2 class="item-area-title"><kk:msg  key="product.details.body.related"/></h2>	 
	    					<span id="kk-up-rc" class="item-vert-arrow previous-items-up"></span>  				
	    					<span id="kk-down-rc" class="item-vert-arrow next-items-down"></span>  				
	    				</div>
	   					<div id="related-carousel" class="items jcarousel-skin-kk">
	   							<ul>
		  						<% for (int i = 0; i < prodMgr.getAllRelatedProducts().length; i++){ %>
									<% com.konakart.appif.ProductIf relatedProd = prodMgr.getAllRelatedProducts()[i];%>
									<li><kk:prodTile prod="<%=relatedProd%>" style="small"/></li>
								<%}%>	
								</ul> 					
	   					</div>
	    			</div>
    			<%}%>
  	   			<%if (prodMgr.getAlsoPurchased() != null && prodMgr.getAlsoPurchased().length > 0) { %>
	    			<div id="also-purchased-items" class="item-area-sidebar rounded-corners">
	    				<div class="item-area-header">
	    					<h2 class="item-area-title"><kk:msg  key="also.purchased.body.title"/></h2>	 
	    					<span id="kk-up-ab" class="item-vert-arrow previous-items-up"></span>  				
	    					<span id="kk-down-ab" class="item-vert-arrow next-items-down"></span>  				
	    				</div>
	   					<div id="also-bought-carousel" class="items jcarousel-skin-kk">
	   							<ul>
		  						<% for (int i = 0; i < prodMgr.getAlsoPurchased().length; i++){ %>
									<% com.konakart.appif.ProductIf alsoProd = prodMgr.getAlsoPurchased()[i];%>
									<li><kk:prodTile prod="<%=alsoProd%>" style="small"/></li>
								<%}%>	
								</ul> 					
	   					</div>
	    			</div>
    			<%}%>
			</div>
		</div>
<% } %>    	