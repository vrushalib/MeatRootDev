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
<% com.konakart.appif.WishListIf[] wishListArray = wishListMgr.getCurrentWishLists();%>

 				<h1 id="page-title"><kk:msg  key="show.giftregistries.body.weddinglists.found"/></h1>			
	    		<div id="show-gift-registries" class="content-area rounded-corners">
	    			<%if (wishListArray == null || wishListArray.length == 0){ %>
						<kk:msg  key="show.giftregistries.body.no.weddinglists"/>.
					<%} else { %>				
	    		
	    				<div class="wish-list-navigation">
	    					<div class="wish-list-navigation-left">
		    					<span class="number-of-items navigation-element"><%=wishListMgr.getCurrentWishListOffset() + 1%>-<%=wishListMgr.getNumberOfWishLists() + wishListMgr.getCurrentWishListOffset()%> <kk:msg  key="common.of"/> <%=wishListMgr.getTotalNumberOfWishLists()%></span>				    					
			    				<span class="separator"></span>
							</div>
							<div class="wish-list-navigation-right">
	    						<kk:paging pageList="<%=wishListMgr.getWishListPageList()%>" currentPage="<%=wishListMgr.getCurrentWishListPage()%>" showBack="<%=wishListMgr.getWishListShowBack()%>" showNext="<%=wishListMgr.getWishListShowNext()%>" action="NavigateWishList" timestamp="0"></kk:paging>
							</div>
						</div>
						<div class="registry-list">
								<%int numRegistries =  (wishListMgr.getCurrentWishLists().length > wishListMgr.getMaxRows() ) ? wishListMgr.getMaxRows() :wishListMgr.getCurrentWishLists().length;%>
								<% for (int i = 0; i <numRegistries; i++){ %>																	
									<% com.konakart.appif.WishListIf wl = wishListArray[i];%>
									<div class="single-registry <%=(i%2==0)?"even":"odd"%>">
										<div class="left-col">
											<div class="registry-label"><kk:msg  key="show.giftregistries.body.eventname"/></div>
											<div class="registry-attr"><%=wl.getName()%></div>
											<div class="registry-label"><kk:msg  key="show.giftregistries.body.groomname"/></div>
											<div class="registry-attr"><%=wl.getCustomerFirstName() %>&nbsp;<%=wl.getCustomerLastName() %></div>
											<div class="registry-label"><kk:msg  key="show.giftregistries.body.bridename"/></div>
											<div class="registry-attr"><%=wl.getCustomer1FirstName() %>&nbsp;<%=wl.getCustomer1LastName() %></div>
											<div class="registry-label"><kk:msg  key="show.giftregistries.body.eventdate"/></div>
											<div class="registry-attr"><%=kkEng.getDateAsString(wl.getEventDate())%></div>
											<%if (wl.getLinkUrl() != null && wl.getLinkUrl().length() > 0) {%>
												<div class="registry-label"><kk:msg  key="create.gift.registry.body.event.link.url"/></div>
												<div class="registry-attr"><a class="text-link" href="<%=wl.getLinkUrl()%>"><%=wl.getLinkUrl()%></a></div>
											<%}%>
										</div>
										<div class="right-col">
											<a href='<%="ShowGiftRegistryItems.action?wishListId="+wl.getId()%>' class="button-medium small-rounded-corners">
												<span><kk:msg  key="common.select"/></span>
											</a>
										</div>
									</div>
				    			<% } %>
						</div>
	    				<div class="wish-list-navigation">
	    					<div class="wish-list-navigation-left">
		    					<span class="number-of-items navigation-element"><%=wishListMgr.getCurrentWishListOffset() + 1%>-<%=wishListMgr.getNumberOfWishLists() + wishListMgr.getCurrentWishListOffset()%> <kk:msg  key="common.of"/> <%=wishListMgr.getTotalNumberOfWishLists()%></span>				    					
			    				<span class="separator"></span>
							</div>
							<div class="wish-list-navigation-right">
	    						<kk:paging pageList="<%=wishListMgr.getWishListPageList()%>" currentPage="<%=wishListMgr.getCurrentWishListPage()%>" showBack="<%=wishListMgr.getWishListShowBack()%>" showNext="<%=wishListMgr.getWishListShowNext()%>" action="NavigateWishList" timestamp="0"></kk:paging>
							</div>
						</div>
					<%}%>
					<div class="form-buttons-wide">
						<a href="GiftRegistrySearch.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
					</div>
		    	</div>

