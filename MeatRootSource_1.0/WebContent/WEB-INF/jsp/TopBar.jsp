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
<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% int basketItems = kkEng.getBasketMgr().getNumberOfItems();%>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf currentCustomer = customerMgr.getCurrentCustomer();%>

<%int wishlistItems = 0;%>
<%if (currentCustomer.getWishLists() != null){%>
	<% for (int i = 0; i < currentCustomer.getWishLists().length; i++){ %>
		<% com.konakart.appif.WishListIf wishList = currentCustomer.getWishLists()[i];%>
		<%if (wishList.getListType()== com.konakart.al.WishListMgr.WISH_LIST_TYPE && wishList.getWishListItems()!= null){%>
			<%wishlistItems = wishList.getWishListItems().length;%>
		<%}%>
	<%}%>		
<%}%>		


<script type="text/javascript">	
$(function() {
	$('#lang-select').selectBoxIt({
		downArrowIcon: "selectboxit-down-arrow"
    });	 	
	$('#currency-select').selectBoxIt({
		downArrowIcon: "selectboxit-down-arrow"
    });	 
});
</script>

<div id="top-bar-container">
  	<div id="top-bar">
  		<div id="options">
	  		<div id="language-selector" class="top-bar-menu-item">
	  			<form action="SetLocale.action" method="post">  
	  				<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
					<select id="lang-select" name="locale"  onchange="submit()">
						<option  value="en_GB" data-icon="flag flag-en-GB" <%=kkEng.getLocale().equals("en_GB")?"selected=\"selected\"":""%>>English</option>
						<option  value="de_DE" data-icon="flag flag-de-DE"  <%=kkEng.getLocale().equals("de_DE")?"selected=\"selected\"":""%>>Deutsch</option>
						<option  value="es_ES" data-icon="flag flag-es-ES"  <%=kkEng.getLocale().equals("es_ES")?"selected=\"selected\"":""%>>Español</option>
						<option  value="pt_BR" data-icon="flag flag-pt-BR"  <%=kkEng.getLocale().equals("pt_BR")?"selected=\"selected\"":""%>>Português</option>
					</select>
				</form>									
	  		</div>
	  		<div id="currency-selector"  class="top-bar-menu-item">
				<form action="SelectCurrency.action" method="post">  
					<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
					<select id="currency-select" name="currencyCode"  onchange="submit()">
						<% for (int i = 0; i < kkEng.getCurrencies().length; i++){ %>
							<% com.konakart.appif.CurrencyIf currency = kkEng.getCurrencies()[i];%>
							<% if (currency != null) { %>
								<option  value="<%=currency.getCode()%>"><%=currency.getTitle()%></option>
							<% } %>
						<% } %>
					</select>
				</form>										  			
	  		</div>
	  		<%if (kkEng.getSessionId() != null && kkEng.getSessionId().length() > 0) {%>	
		  		<div  class="top-bar-menu-item">
		  			<a href="LogOut.action" class="header2-top"><kk:msg  key="header.logout.page"/></a>
		  		</div>
			<% } %>	  		
	  		<div id="my-account" class="top-bar-menu-item">
	  			<a href="LogIn.action"><kk:msg  key="header.my.account"/></a>
	  		</div>
	  		<%if (kkEng.getConfigAsBoolean("ENABLE_GIFT_REGISTRY", false)) {%>	
		  		<div id="gift-registry" class="top-bar-menu-item">	  			
		  			<a href="GiftRegistrySearch.action" class="header2-top"><kk:msg  key="header.gift.registries"/></a>
		  		</div>	
			<%}%>	
			<%if (kkEng.isWishListEnabled()) { %>
		  		<div id="wish-list" class="top-bar-menu-item">	  			
		  			<kk:msg  key="wishlist.tile.wishlist"/>
			  		<%if (wishlistItems > 0) { %>			 	
						(<%=wishlistItems%>)
					<%}%>				  		
		  		</div>	
			   	<div id="wish-list-container">
			   		<div id="wish-list-mouseover-shadow" class="slide-out-shadow"></div>
				  	<div id="wish-list-contents" class="slide-out-contents shadow">			  	
			  			<%if (wishlistItems==0){%>
			  				<kk:msg  key="wishlist.tile.empty"/>
			  			<%}else{%>
					  		<div id="wish-list-items">	
					  			<% com.konakart.appif.WishListIf selectedWishList = null;%>
								<% for (int i = 0; i < currentCustomer.getWishLists().length; i++){ %>		
									<% com.konakart.appif.WishListIf wishList = currentCustomer.getWishLists()[i];%>			
									<%if (wishList.getListType()== com.konakart.al.WishListMgr.WISH_LIST_TYPE){%>
										<% selectedWishList = wishList; %>
										<%if (wishList.getWishListItems() != null && wishList.getWishListItems().length > 0){%>
											<% for (int j = 0; j < wishList.getWishListItems().length; j++){ %>
												<% com.konakart.appif.WishListItemIf item = wishList.getWishListItems()[j];%>
												<%if (item.getProduct() != null) { %>		
													<div class="shopping-cart-item">
											  			<a href='<%="SelectProd.action?prodId="+item.getProduct().getId()%>'><img src="<%=kkEng.getProdImage(item.getProduct(), com.konakart.al.KKAppEng.IMAGE_TINY)%>" border="0" alt="<%=item.getProduct().getName()%>" title=" <%=item.getProduct().getName()%> "></a>
											  			<a href='<%="SelectProd.action?prodId="+item.getProduct().getId()%>' class="shopping-cart-item-title"><%=item.getProduct().getName()%></a>
										  				<kk:prodOptions options="<%=item.getOpts()%>"/>
											  			<div class="shopping-cart-item-price">
												  			<%if (kkEng.displayPriceWithTax()) { %>		
												  				<%=kkEng.formatPrice(item.getFinalPriceIncTax())%>
												  			<%}else{%>
												  				<%=kkEng.formatPrice(item.getFinalPriceExTax())%>
												  			<%}%>					  			
											  			</div>
									  				</div>							
												<%}%>										
											<%}%>
										<%}%>		    																											
									<%}%>
								<%}%>
						  	</div>	
						  	<div id="wish-list-subtotal">
						  		<div class="subtotal">
						  			<div class="subtotal-label"><kk:msg  key="common.subtotal"/></div>
									<%if (kkEng.displayPriceWithTax()){%>
										<div class="subtotal-amount"><%=kkEng.formatPrice(selectedWishList.getFinalPriceIncTax())%></div>
									<%}else{%>
										<div class="subtotal-amount"><%=kkEng.formatPrice(selectedWishList.getFinalPriceExTax())%></div>
									<%}%>		    																											
						  		</div>
						  	</div>
				  		<%}%>				  				  	
				  	</div>	
				</div> 
			<%}%>
			<div id="shopping-cart" class="top-bar-menu-item">
		  		<kk:msg key="cart.tile.shoppingcart"/>
		  		<%if (basketItems > 0) { %>			 	
					(<%=basketItems%>)
				<%}%>
		  	</div>
		    <div id="shopping-cart-container">
			  	<div id="shopping-cart-mouseover-shadow" class="slide-out-shadow"></div>
			  	<div id="shopping-cart-contents" class="slide-out-contents shadow">
					<%if (basketItems==0 || customerMgr.getCurrentCustomer()==null || customerMgr.getCurrentCustomer().getBasketItems()==null) { %>	
						<kk:msg  key="cart.tile.empty"/>
					<%}else{%>
				  		<div id="shopping-cart-items">	
								<% for (int i = 0; i < customerMgr.getCurrentCustomer().getBasketItems().length; i++){ %>
									<% com.konakart.appif.BasketIf item = customerMgr.getCurrentCustomer().getBasketItems()[i];%>
									<%if (item.getProduct() != null) { %>		
										<div class="shopping-cart-item">
								  			<a href='<%="SelectProd.action?prodId="+item.getProduct().getId()%>'><img src="<%=kkEng.getProdImage(item.getProduct(), com.konakart.al.KKAppEng.IMAGE_TINY)%>" border="0" alt="<%=item.getProduct().getName()%>" title=" <%=item.getProduct().getName()%> "></a>
								  			<a href='<%="SelectProd.action?prodId="+item.getProduct().getId()%>' class="shopping-cart-item-title"><%=item.getProduct().getName()%></a>
							  				<kk:prodOptions options="<%=item.getOpts()%>"/>
								  			<div class="shopping-cart-item-price">
									  			<%if (kkEng.displayPriceWithTax()) { %>		
									  				<%=kkEng.formatPrice(item.getFinalPriceIncTax())%>
									  			<%}else{%>
									  				<%=kkEng.formatPrice(item.getFinalPriceExTax())%>
									  			<%}%>
									  			&nbsp;<kk:msg  key="cart.tile.quantity"/>:<%=item.getQuantity()%>					  			
								  			</div>
						  				</div>							
									<%}%>
								<%}%>
					  	</div>
					  	<div id="subtotal-and-checkout">
					  		<div class="subtotal">
					  			<div class="subtotal-label"><kk:msg  key="common.subtotal"/></div>
					  			<div class="subtotal-amount"><%=kkEng.getBasketMgr().getFormattedBasketTotal()%></div>
					  			<div id="shopping-cart-checkout-button" class="button small-rounded-corners"><kk:msg  key="common.checkout"/></div>
					  		</div>
					  	</div>					
					<%}%>				  		
			  	</div>	
			</div>
		</div>
  	</div>
</div>  