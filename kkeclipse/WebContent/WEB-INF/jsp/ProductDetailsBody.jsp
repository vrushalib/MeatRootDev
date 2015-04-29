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
<% String title = "";%>


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
				<div id="buying-details-mobile" class="rounded-corners-top">
					<%kkEng.setCustomTemp1("small"); %>
					<tiles:insertAttribute name="pdInclude" /> 
				</div>
				<div id="share-this-mobile" class="rounded-corners-bottom">
    				<span id="share-this-label"><kk:msg  key="product.details.body.share.this"/>:</span>
    				<!-- AddThis Button BEGIN -->
					<div class="addthis_toolbox addthis_default_style addthis_32x32_style">
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
	    		<div id="product-content">
	    			<div id="product-content-tabs">
		    			<div id="product-description-tab" class="<%=revMgr.isShowTab()?"":"selected-product-content-tab"%> product-content-tab small-rounded-corners-top"><kk:msg  key="product.details.body.product.description"/></div>		
    			    	<div class="product-content-tab-spacer"></div>
		    			<div id="product-specifications-tab" class="product-content-tab small-rounded-corners-top"><kk:msg  key="product.details.body.specifications"/></div>
		    			<div class="product-content-tab-spacer"></div>
				    	<div id="product-reviews-tab" class="<%=revMgr.isShowTab()?"selected-product-content-tab":""%> product-content-tab small-rounded-corners-top"><kk:msg  key="product.details.body.reviews"/> (<%=prod.getNumberReviews()%>)</div>
		    			<div class="product-content-tab-filler"></div>
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
													<span class="<%=(search!=null&&search.getRating()==i)?"kk-selected":"kk-not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
												</a>
											<%}else{%>
												<span class="<%=(search!=null&&search.getRating()==i)?"kk-selected":"kk-not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
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
						<div class="product-reviews-navigation-mobile">
	    					<div class="product-reviews-navigation-top">
		    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>	
		    					<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>			    					
							</div>
							<div class="product-reviews-navigation-bottom">
								<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>" type="small"/>			    				
			    				<kk:revOrderBy/>
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
	    				<div class="product-reviews-navigation-mobile">
	    					<div class="product-reviews-navigation-top">
		    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>	
		    					<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>			    					
							</div>
							<div class="product-reviews-navigation-bottom">
								<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>" type="small"/>			    				
			    				<kk:revOrderBy/>
	    					</div>		
						</div>
					<% } else { %>
						<p style="clear:both"><a href='<%="WriteReview.action?prodId="+prod.getId()%>' class="text-link"><kk:msg  key="show.reviews.body.be.first"/></a></p>
					<% } %>		
	    							
	    			</div>	  
	    		</div>
	    		<div id="product-content-mobile" class="<%=revMgr.isShowTab()?"accordion-show-reviews":""%>">
		    		<%revMgr.setShowTab(false);%>	    		
	    			<span class="header"><kk:msg  key="product.details.body.product.description"/></span>
	    			<div id="product-description">
	    				<p style="clear:both"><%=prod.getDescription()%></p>		    			
	    			</div>
					<span class="header"><kk:msg  key="product.details.body.specifications"/></span>
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
	    			<span class="header"><kk:msg  key="product.details.body.reviews"/> (<%=prod.getNumberReviews()%>)</span>

 	    			
 	    			<%if (prod.getNumberReviews() > 0){ %>
	 	    			<div id="product-reviews">
		    				<div id="average-customer-reviews">
			    				<div id="average-customer-reviews-meters">
			    					<% for (int i = 5; i > 0; i--){ %>
			    						<%int qty = revMgr.getRatingQuantity()[i-1]; %>
				    					<div class="average-customer-reviews-meter-container">
				    						<div class="average-customer-reviews-meter-label">
				    							<%if (qty > 0){ %> 
													<a href='<%="FilterRev.action?rating="+i%>'>
														<span class="<%=(search!=null&&search.getRating()==i)?"kk-selected":"kk-not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
													</a>
												<%}else{%>
													<span class="<%=(search!=null&&search.getRating()==i)?"kk-selected":"kk-not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
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
							<div class="product-reviews-navigation-mobile">
		    					<div class="product-reviews-navigation-top">
			    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>	
			    					<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>			    								    										
								</div>
								<div class="product-reviews-navigation-bottom">
									<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>" type="small"/>			    				
			    					<kk:revOrderBy/>
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
		    				<div class="product-reviews-navigation-mobile">
		    					<div class="product-reviews-navigation-top">
			    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>	
			    					<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>			    								    											
								</div>
								<div class="product-reviews-navigation-bottom">
									<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>" type="small"/>			    				
			    					<kk:revOrderBy/>
								</div>
		    				</div>	
		    			</div>	
					<% } else { %>
						<div id="product-reviews">
							<p style="clear:both"><a href='<%="WriteReview.action?prodId="+prod.getId()%>' class="text-link"><kk:msg  key="show.reviews.body.be.first"/></a></p>
		    			</div>	
					<% } %>		

				</div> 	
    		</div>
			<div id="product-column-right">
				<div id="buying-details" class="rounded-corners-top">
					<%kkEng.setCustomTemp1("large"); %>
					<tiles:insertAttribute name="pdInclude" /> 
				</div>
				<div id="share-this" class="rounded-corners-bottom">
    				<span id="share-this-label"><kk:msg  key="product.details.body.share.this"/>:</span>
    				<!-- AddThis Button BEGIN -->
					<div class="addthis_toolbox addthis_default_style">
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
    			
 	    		<%title = kkEng.getMsg("product.details.body.related");%>
	 			<kk:vertCarousel prods="<%=prodMgr.getAllRelatedProducts()%>" title="<%=title%>"/>
 
 	    		<%title = kkEng.getMsg("also.purchased.body.title");%>
	 			<kk:vertCarousel prods="<%=prodMgr.getAlsoPurchased()%>" title="<%=title%>"/>   			
 			</div>	
		</div>
	
		<div id="product-area-bottom-mobile">
				
 	    		<%title = kkEng.getMsg("product.details.body.related");%>
	 			<kk:carousel prods="<%=prodMgr.getAllRelatedProducts()%>" title="<%=title%>" width="180" widthSmall="150" breakpointSmall="440"/>
 
 	    		<%title = kkEng.getMsg("also.purchased.body.title");%>
	 			<kk:carousel prods="<%=prodMgr.getAlsoPurchased()%>" title="<%=title%>" width="180" widthSmall="150" breakpointSmall="440"/>
				
 		</div>
		
<% } %>    	