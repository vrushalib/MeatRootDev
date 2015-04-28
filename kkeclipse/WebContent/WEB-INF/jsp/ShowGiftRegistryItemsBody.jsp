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
<% com.konakart.al.WishListMgr wishListMgr = kkEng.getWishListMgr();%>

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
					<p><kk:msg  key="show.giftregistry.items.body.empty.weddinglist"/></p>
				<% } else { %>		
					<div id="wish-list-msg">
		    			<kk:msg  key="show.giftregistry.items.body.instructions"/>
		    		</div>	
		    		<form action="GiftRegistryListSubmit.action" id="form1" method="post" class="form-section">
		    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<s:set scope="request" var="id" value="id"/> 
						<% int id = ((Integer)request.getAttribute("id")).intValue();%>
						<input type="hidden" id="wish-list-id" name="id" value="<%=id%>"/>
	    				<table>
	    					<thead>
	    						<tr>
	    							<td class="image-col"><kk:msg  key="common.item"/></td>
	    							<td class="item-col"></td>
	    							<td class="priority-col"><kk:msg  key="edit.wishlist.body.priority"/></td>
	    							<td class="q-desired-col"><kk:msg  key="edit.wishlist.body.q.desired"/></td>
	    							
	    							<td class="price-col right"><kk:msg  key="edit.wishlist.body.price"/></td>
	    							<td class="add-to-cart-col center"></td>

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
											<% if (item.getPriority() == 1 ){%>
												<kk:msg  key="common.lowest"/>
											<%  } else if (item.getPriority() == 2 ){ %>
												<kk:msg  key="common.low"/>
											<%  } else if (item.getPriority() == 3 ){ %>
												<kk:msg  key="common.medium"/>
											<%  } else if (item.getPriority() == 4 ){ %>
												<kk:msg  key="common.high"/>
											<%  } else if (item.getPriority() == 5 ){ %>
												<kk:msg  key="common.highest"/>
											<%  }%>
		    							</td>
		    							<td>
											<span class="qty-desired"><span class="qty-desired-label"><kk:msg  key="show.giftregistry.items.body.desired"/>:</span> <%=item.getQuantityDesired() %></span>
		    					    		<span class="qty-received"><span class="qty-received-label"><kk:msg  key="show.giftregistry.items.body.received"/>:</span> <%=item.getQuantityReceived() %></span>
		    							</td>

		    							<td class="right">
											<%if (kkEng.displayPriceWithTax()){%>
												<%=kkEng.formatPrice(item.getTotalPriceIncTax() )%>
											<%}else{%>
												<%=kkEng.formatPrice(item.getTotalPriceExTax())%>
											<%}%>		    																		
		    							</td>	    							
		    							<td class="right">
											<% if (item.getQuantityReceived() < item.getQuantityDesired() && kkEng.getQuotaMgr().canAddToBasket(item.getProdId(), null) > 0){%>
												<a href='<%="GiftRegistryListSubmit.action?action=a&wid="+id+"&id="+item.getWishListItemId()%>' class="button-small small-rounded-corners add-to-cart-text"><kk:msg  key="common.add.to.cart"/></a>		
												<a href='<%="GiftRegistryListSubmit.action?action=a&wid="+id+"&id="+item.getWishListItemId()%>' class="fa fa-shopping-cart button-small small-rounded-corners add-to-cart-icon" ></a>								
											<%}%>
		    							</td>
		    					    </tr>
		    					    <% k++; %>	
								<%}%>
	    					</tbody>	    				
	    				</table>
		    			<div class="gift-registry-navigation">
	    					<div class="gift-registry-navigation-left">
		    					<span class="number-of-items navigation-element"><%=wishListMgr.getCurrentItemOffset() + 1%>-<%=wishListMgr.getNumberOfWishListItems() + wishListMgr.getCurrentItemOffset()%> <kk:msg  key="common.of"/> <%=wishListMgr.getTotalNumberOfWishListItems()%></span>				    					
			    				<span class="separator"></span>
							</div>
							<div class="gift-registry-navigation-right">
		    					<span class="show-per-page navigation-element navigation-dropdown"></span>
			    				<span class="separator"></span>
	    						<kk:paging pageList="<%=wishListMgr.getItemPageList()%>" currentPage="<%=wishListMgr.getCurrentItemPage()%>" showBack="<%=wishListMgr.getItemShowBack()%>" showNext="<%=wishListMgr.getItemShowNext()%>" action="NavigateGiftRegistryItems" timestamp="0"></kk:paging>
							</div>
						</div>
						<div >
							<a href="Welcome.action" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="edit.cart.body.continue.shopping"/></span></a>						
						</div>
					</form>		
				<% } %>	    	
	    		</div>
	    	</div>	
