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
<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>
<% com.konakart.al.WishListMgr wishListMgr = kkEng.getWishListMgr();%>
<% com.konakart.appif.WishListIf wishList = wishListMgr.getCurrentWishList();%>

 				<h1 id="page-title"><kk:msg  key="change.gift.registry.address.body.shippingaddress"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
							<div class="form-section">
								<div class="form-section-title no-margin">
									<h3><kk:msg  key="address.book.body.addressbookentries"/></h3>									
								</div>
								<%if (cust.getAddresses() != null && cust.getAddresses().length > 0){ %>
									<% for (int i = 0; i < cust.getAddresses().length; i++){ %>
										<% com.konakart.appif.AddressIf addr = cust.getAddresses()[i];%>						
										<div class="select-addr-section <%=(i%2==0)?"even":"odd"%>">
											<div class="select-addr">
												<%if (i == 0){ %>
													<span class="primary-addr-label">(<kk:msg  key="address.book.body.primaryaddress"/>)</span><br>
												<% } %>											
												<%=kkEng.removeCData(addr.getFormattedAddress())%>
											</div>
											<div class="select-addr-buttons">
												<a href='<%="ChangeGiftRegistryAddrSubmit.action?addrId="+addr.getId()%>' class="button small-rounded-corners">
													<span><kk:msg  key="common.select"/></span>
												</a>									
											</div>
										</div>
									<%}%>
								<%}%>
							</div>
							<div class="form-buttons-wide">
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
			    	</div>
	    		</div>


