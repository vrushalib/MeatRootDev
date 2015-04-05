	var updateCartRequestJSON = {};
	var isRemoveClicked = false;
	var isCartUpdated = false;

/*
 * Sends an AJAX request to a struts action
 */
function callAction(parmArray, callback, url) {
	
	
	if (document.getElementById('kk_portlet_id')) {
		AUI().ready('liferay-portlet-url', function(A) { 
	        var renderURL = Liferay.PortletURL.createResourceURL();
	        renderURL.setParameter("struts.portlet.action", "/" + url);
	        renderURL.setPortletId(document.getElementById('kk_portlet_id').value);
	        renderURL.setWindowState("exclusive");
	        if (parmArray) {
				for ( var i = 0; i < parmArray.length; i=i+2) {
					renderURL.setParameter(parmArray[i], parmArray[i+1]);
				}
				renderURL.setParameter("xsrf_token", document.getElementById('kk_xsrf_token').value);
			}
	        url = renderURL.toString();
	        
			$.ajax({
				type : 'POST',
				//timeout : '20000',
				scriptCharset : "utf-8",
				contentType : "application/json; charset=utf-8",
				url : url,
				data : null,
				success : callback,
				error : function(jqXHR, textStatus, errorThrown) {
					var errorMsg = "JSON API call to the URL " + url
							+ " wasn't successful.";
					if (textStatus != null && textStatus != '') {
						errorMsg += "\nStatus:\t" + textStatus;
					}
					if (errorThrown != null && errorThrown != '') {
						errorMsg += "\nError:\t" + errorThrown;
					}
					alert(errorMsg);
				},
				dataType : 'json'
			});
		});		
	} else {
		
		var parms='{"":""}';
        if (parmArray) {
        	parms = '{';
			for ( var i = 0; i < parmArray.length; i=i+2) {
				
				parms = parms + '"' + parmArray[i]+'":"'+ parmArray[i+1]+ '"';
				if (i+2 < parmArray.length) {
					parms = parms + ',';
				}
			}
			parms = parms + ',"xsrf_token":"'+ document.getElementById('kk_xsrf_token').value + '"';
	        parms = parms + '}';
		}
		$.ajax({
			type : 'POST',
			timeout : '20000',
			scriptCharset : "utf-8",
			contentType : "application/json; charset=utf-8",
			url : url,
			data : parms,
			dataType : 'json',
			success : callback,
			error : function(jqXHR, textStatus, errorThrown) {
				var errorMsg = "JSON API call to the URL " + url
						+ " wasn't successful.";
				if (textStatus != null && textStatus != '') {
					errorMsg += "\nStatus:\t" + textStatus;
				}
				if (errorThrown != null && errorThrown != '') {
					errorMsg += "\nError:\t" + errorThrown;
				}
				alert(errorMsg);
			}
		});
	}
}

/*
 * Derives a portlet url
 */
function getURL(action, parmArray) {
	
	if (document.getElementById('kk_portlet_id')) {
		var id = document.getElementById('kk_portlet_id').value;
		var sampleURL = document.getElementById('kk_sample_url').value;
		
		var url = sampleURL.replace("KK_ACTION",action);
		if (parmArray) {
			for ( var i = 0; i < parmArray.length; i=i+2) {
				var name = parmArray[i];
				var val = parmArray[i+1];
				var add = '&_'+id+'_'+name+'='+val;
				url = url + add;
			}		
		}
		return url;		
	} else {
		var url = action;
		if (parmArray) {
			for ( var i = 0; i < parmArray.length; i=i+2) {
				var name = parmArray[i];
				var val = parmArray[i+1];
				var add="";
				if (i==0) {
					add += '?';
				} else {
					add += '&';
				}				
				add = add + name+'='+val;
				url = url + add;
			}		
		}
		return url;
	}
}

/*
 * Suggested search code used in Header.jsp. Figure out which search to do based
 * on value in key.
 */
function kkSearch() {

	// Get key and search string from page
	var key = document.getElementById('kk_key').value;
	var text = document.getElementById('search-input').value;

	if (key != null && key.length > 0) {
		var keyArray = key.split(',');
		if (keyArray.length == 3) {
			var manuId = keyArray[1];
			var catId = keyArray[2];
			if (catId > -1 && manuId > -1) {
				// Search category and manufacturer
				document.getElementById('manuId').value = manuId;
				document.getElementById('catId').value = catId;
				document.getElementById('ssForm').action = getURL("SelectCat.action");
				document.getElementById('ssForm').submit();
			} else if (catId > -1) {
				// Search cat
				document.getElementById('manuId').value = "-1";
				document.getElementById('catId').value = catId;
				document.getElementById('ssForm').action = getURL("SelectCat.action");
				document.getElementById('ssForm').submit();
			} else if (manuId > -1) {
				// Search manufacturer
				document.getElementById('manuId').value = manuId;
				document.getElementById('ssForm').action = getURL("ShowSearchByManufacturerResultsByLink.action");
				document.getElementById('ssForm').submit();
			} else {
				// Search based on text
				document.getElementById('searchText').value = text;
				document.getElementById('ssForm').action = getURL("QuickSearch.action");
				document.getElementById('ssForm').submit();
			}
		}
	} else if (text != null && text.length > 0) {
		/*
		 * Reach here if someone has entered free text and clicked the search
		 * button or the enter key. Rather than doing a search on the text we
		 * see if there is a suggested search hit and then use the extra
		 * information returned from the suggested search hit to provide better
		 * results. i.e. It provides results for a category search whereas a
		 * simple search wouldn't show any results.
		 */
		callAction(new Array("term", text), suggestedSearchCallback,
				getURL("SuggestedSearch.action"));
	}
}

/*
 * Callback for suggested search
 */
var suggestedSearchCallback = function(result, textStatus, jqXHR) {
	if (result != null && result.length > 0) {
		document.getElementById('kk_key').value = result[0].id;
		document.getElementById('search-input').value = result[0].value;
		kkSearch();
	} else {
		var text = document.getElementById('search-input').value;
		document.getElementById('searchText').value = text;
		document.getElementById('ssForm').action = getURL("QuickSearch.action", new Array("searchInDesc","true"));
		document.getElementById('ssForm').submit();
	}
};

/*
 * Reset key id since user has typed into search box
 */
function kkKeydown() {
	document.getElementById('kk_key').value = "";
}

/*
 * Used by address maintenance panels
 */
function changeCountry() {
	if (document.getElementById('state')) {
		document.getElementById('state').value="";
	}	
	document.getElementById('countryChange').value="1";
	document.getElementById('form1').submit();
}

$(function() {
	
	$("#shopping-cart").click(goToCartPage);
	$("#wish-list").click(goToWishListPage);
	
	$(".item-over").click(function() {
		var prodId = (this.id).split('-')[1];
		goToProdDetailsPage(prodId);
	});	
	
	/*
	 * Hover effects for Add To Cart button
	 */	
	$(".item").not(".style-small").hover(
			function() {
				$(this).addClass("item-over-container");
				$(this).find(".item-over").show();
			}, function() {
				$(this).removeClass("item-over-container");
				$(this).find(".item-over").hide();
			});

	$('.add-to-cart-qty').click(function (evt) {
	    evt.stopPropagation();

	   
	});
	
	/*
	 * Hover effects for Sliding Cart 
	 */
	var cartHover=0;	
	$("#shopping-cart").hover(
			function() {
				// in
				cartHover=1;
				showCart("#shopping-cart");
			}, function() {				
				// out
				setTimeout(function(){
					if (cartHover!=2) {
						cartHover=0;
						hideCart("#shopping-cart");
					}
					}, 500);
			});
	$("#shopping-cart-container").hover(
			function() {
				// in
				cartHover=2;
				showCart("#shopping-cart");		
			}, function() {
				// out
				cartHover=0;
				hideCart("#shopping-cart");
				updateCartDetails();
			});
	
	/*
	 * Initialise wish list position and visibility 
	 */
	setWishListPosition();
		
	/*
	 * Hover effects for Sliding Wish list 
	 */
	var wlHover=0;
	$("#wish-list").hover(
			function() {
				// in
				wlHover=1;
				showWishList("#wish-list");
			}, function() {				
				// out
				setTimeout(function(){
					if (wlHover!=2) {
						wlHover=0;
						hideWishList("#wish-list");
					}
					}, 500);				
			});
	$("#wish-list-container").hover(
			function() {
				// in
				wlHover=2;
				showWishList("#wish-list");		
			}, function() {
				// out
				wlHover=0;
				hideWishList("#wish-list");
			});
	/*
	 * Add to Cart
	 */
	$(document).ready(function(){
		
	$(".add-to-cart-button")
	.click(
			function() {
				
				var prodId = (this.id).split('-')[1];			
				
				 var id = $("#prodQuantityId_"+prodId).val();			
				
				callAction(new Array("prodId",prodId,"id",id), 
						addToCartCallback,
						"AddToCartFromProdId.action?id="+id);
				
				/*$("span#"+this.id).show();*/
				
				return false;
			});
	});
	
	/*
	 * Add to Wish List
	 */
	$(".add-to-wishlist")
			.click(
					function() {
						var prodId = (this.id).split('-')[1];
						callAction(new Array("prodId",prodId), 
								 addToWishListCallback,
								"AddToWishListFromProdId.action");
						return false;
					});
	
	/*
	 * Subscribe to newsletter
	 */
	$("#newsletter-button").click(submitNewsletterForm);
	
	/*
	 * Basket checkout button on fade in / out basket widget
	 */
	$("#shopping-cart-checkout-button").click(goToCheckoutPage);
	
	/*
	 * Tooltips
	 */
	$(".has-tooltip").tooltip();
	
	/*
	 * Agree to use of cookies
	 */
	$("#cookie-warn-button")
	.click(
			function() {
				callAction(null, 
						agreeToCookiesCallback,
						"AgreeToCookies.action");
				return false;
			});
	
	/*
	 * Add postcode suggested by user
	 */
	$('#go')
	.click(
			function(){
				$("#pincode_area").hide();
				$("#email_area").show();
			});
	
	$("#back")
	.click(
			function(){
				$("#message_area").hide();
				$("#error_message").hide();
				$("#success_message").hide();
				$("#pincode_area").show();
			});
	
	$("#done")
	.click(
			function(){
				var pincode = $("#pincode").val().trim();
				var emailId = $("#emailId").val().trim();
				var valid = validate(pincode, emailId);
				$("#email_area").hide();
				$("#message_area").show();
				if(valid){
				callAction(new Array("pincode", pincode, "emailId", emailId), suggestedAreaCallback, "SuggestedArea.action");
					$("#success_message").show();
					$("#pincode").val("");
					$("#emailId").val("");
				}
				else{
					$("#error_message").show();
				}
			});
});

var suggestedAreaCallback = function(result, textStatus, jqXHR) {
	$("#success_message").show();
	$("#pincode").val("");
	$("#emailId").val("");
};


function validate(pincode, emailId){
	if(pincode == "" || emailId == ""){
		return false;
	}
	var mailformat = /^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
	var pinformat = /^\d{6}$/;
	if(mailformat.test(emailId) && pinformat.test(pincode)){
		return true;
	}
	return false;
	return true;
}

/*
 * Submits the sign up to newsletter form
 */
function submitNewsletterForm() {
	var email = $("#newsletter-input").val();
	callAction(new Array("emailAddr", email), 
			 subscribeNewsletterCallback,
			"SubscribeNewsletter.action");
	return false;
}

/*
 * Set the position of the wish list slide down control
 */
function setWishListPosition() {
	if ($("#wish-list").length) {		
		$("#wish-list-container").hide();
		var shadowWidth  =  $("#wish-list-mouseover-shadow").width();
		var space = $("#shopping-cart").position().left - $("#wish-list").position().left-$("#wish-list").width();
		var cartWidth = $("#shopping-cart").width();
		$("#wish-list-mouseover-shadow").css("right", cartWidth+space/2-shadowWidth);
		$("#wish-list-contents").css("right", cartWidth+space/2);	
	}
}

/*
 * Redirect functions
 */
function goToCartPage() {
	return redirect(getURL("ShowCartItems.action"));
}

function goToCheckoutPage() {
	return redirect(getURL("Checkout.action"));
}

function goToLoginPage() {
	return redirect(getURL("LogIn.action"));
}

function goToWishListPage() {
	return redirect(getURL("ShowWishListItems.action"));
}

function goToProdDetailsPage(prodId){
	return redirect(getURL("SelectProd.action", new Array("prodId",prodId)));
}

function redirect(action) {
	if (document.getElementById('kk_portlet_id')) {
		window.location = action;
	} else {
		var base = $('base').attr('href');
		var redirectUrl = base+action;
		window.location = redirectUrl;
	}
	return false;
}


/*
 * Code to display the slide out cart
 */
function showCart(cart) {
	$(cart).addClass("small-rounded-corners-top shopping-cart-mouseover");
	$("#shopping-cart-container").css("display","inline");
}

/*
 * Code to hide the slide out cart
 */
function hideCart(cart) {
	$("#shopping-cart-container").hide();
	$(cart).removeClass("shopping-cart-mouseover small-rounded-corners-top");
}

/*
 * Code to display the slide out wish list
 */
function showWishList(wishList) {
	$(wishList).addClass("small-rounded-corners-top shopping-cart-mouseover");	
	$("#wish-list-container").css("display","inline");
}

/*
 * Code to hide the slide out wish list
 */
function hideWishList(wishList) {
	$("#wish-list-container").hide();
	$(wishList).removeClass("small-rounded-corners-top shopping-cart-mouseover");
}

/*
 * Calculate the product image base
 */
function getProdImageBase(prod, base) {
	return base + prod.imageDir + prod.uuid;
}

/*
 * Calculate the product image extension
 */
function getProdImageExtension(prod) {
	if (prod.image) {
		var ret = prod.image.split('.');
		if (ret.length<2) {
			return "";
		}
		return '.' + ret.pop();
	}
	return "";
}

/*
 * Common code called from addtoCart and addToWishlist callbacks
 */
function getProdOptionText(opts, isWishList) {
	var txt = "";
	if (opts != null && opts.length > 0) {
		for ( var j = 0; j < opts.length; j++) {
			var opt = opts[j];
			if (opt.type == 0) { // Simple options
				txt += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.value+'</span>';
			} else if (opt.type == 1 && !isWishList) { // Variable quantity
				txt += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.quantity+' '+opt.value+'</span>';
			} else if (opt.type == 2 && !isWishList) { // Customer price
				txt += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.formattedCustPrice+'</span>';
			} else if (opt.type == 3 && !isWishList) { // Customer text
				txt += '<br><span class="shopping-cart-item-option"> - '+opt.name+': '+opt.customerText+'</span>';
			}					
		}
	}
	return txt;
}

/*
 * Used to view added to cart details in a popup
 */
var addToCartCallback = function(result, textStatus, jqXHR) {

	/*
	 * Go to product details page to choose options
	 */
	if (result.redirectURL != null) {
		if (result.redirectURL == "Login") {
			goToLoginPage();
		} else {
			goToProdDetailsPage(result.redirectURL);
		}		
		return;
	}

	var txt;
	var itemsJSON = { 
		    cartItems : []
	};
	/*
	 * Update cart slide-out with new basket items
	 */		
	if (result.items != null && result.items.length > 0) {
		txt = '<div id="shopping-cart-items">';
		for ( var i = 0; i < result.items.length; i++) {
			var item = result.items[i];
			var cartItemData = {};
			txt += '<div class="shopping-cart-item">';
			//txt += '<a href="'+ getURL("SelectProd.action", new Array("prodId",item.prodId)) +'"><img src="'+item.prodImgSrc+'" border="0" alt="'+item.prodName+'" title="'+item.prodName+'"></a>';
			txt += '<table><tr><td class="shopping-cart" width="80%"><a href="'+ getURL("SelectProd.action", new Array("prodId",item.prodId)) +'" class="shopping-cart-item-title">'+item.prodName+'</a>';
			txt += getProdOptionText(item.opts, /*isWishList*/false);
			txt += '<br/>'+'<div class="shopping-cart-item-price">';
			txt += item.formattedPrice + '</div></td>';
			txt += '<td class="cart-quantity" width="5%">'+item.quantity+'</td>';
			txt += '<td width="5%"><a id="quantity-minus"><img src="images/icons/minus_icon.png" border="0" height="15px" width="15px" alt="Minus" title="Substract"></a></td>';
			txt += '<td width="5%"><a id="quantity-plus"><img src="images/icons/plus_icon.png" border="0" height="15px" width="15px" alt="Add" title="Add"></a></td>';
			txt += '<td width="5%"><a id="remove-item-from-cart"><img src="images/icons/remove_icon.png" border="0" height="15px" width="15px" alt="Remove" title="Remove"></a></td>';
			txt += '</tr></table>';
			txt += '</div>';
			
			cartItemData = createItemJSON(item);
			itemsJSON.cartItems.push(cartItemData);
		}
		txt += '</div>';
		txt += '<div id="subtotal-and-checkout">';
		txt += '<div class="subtotal">';
		txt += result.subtotalMsg+': '+result.basketTotal+'</div><br/>';
		txt += '<div id="shopping-cart-checkout-button" class="button small-rounded-corners">'+result.checkoutMsg+'</div>';
		txt += '</div>';		
	} else {
		txt = result.emptyCartMsg;
	}
	updateCartRequestJSON = itemsJSON;
	
	$("#shopping-cart-contents").html(txt);
	
	/*
	 * Set event code on checkout button
	 */
	$("#shopping-cart-checkout-button").click(goToCheckoutPage);

	/*
	 * To handle "-" click from shopping cart
	 */
	$("#quantity-minus").click(function() {
		if (item != null && item != undefined) {
			if (item.quantity != 1) {
				var updatedQty = item.quantity - 1;
				updateQuantity(item.prodId, updatedQty);
			}
		}
	});

	/*
	 * To handle "+" click from shopping cart
	 */
	$("#quantity-plus").click(function() {
		if (item != null && item != undefined) {
			var updatedQty = item.quantity + 1;
			updateQuantity(item.prodId, updatedQty);
		}
	});
	
	/*
	 * To remove item from shopping cart
	 */
	$("#remove-item-from-cart").click(function() {
		var itemToRemoveIndex = null;
		var itemToRemoveObj = null;
		
		if (item != null && item != undefined) {
			// ToDo: to be optimized
			if ((updateCartRequestJSON != null && updateCartRequestJSON != undefined) 
					&& (updateCartRequestJSON.cartItems != null && updateCartRequestJSON.cartItems != undefined)) {
								
				if (updateCartRequestJSON.cartItems.length > 0) {
					//itemsLabel:
					for (var i = 0; i < updateCartRequestJSON.cartItems.length; i++) {	
						$.each(updateCartRequestJSON.cartItems[i], function(key, value) {
							if(key == "prodId" && value == item.prodId) {
								itemToRemoveIndex = i;
								itemToRemoveObj = updateCartRequestJSON.cartItems[i];
							}
						});
					}
				
					itemToRemoveObj["action"] = "r";
					updateCartRequestJSON.cartItems.splice(itemToRemoveIndex, 1,
							itemToRemoveObj);
					isRemoveClicked = true;
					
					updateCartDetails();
				}
			}
		}
	});
	
	/*
	 * Update cart summary with new basket data
	 */
	txt = result.shoppingCartMsg;
	if (result.numberOfItems > 0) {
		txt += " ("+result.numberOfItems+")";
	} 
	$("#shopping-cart").html(txt);

	/*
	 * Reset the position of the wish list slide out control since
	 * the cart summary length may have changed
	 */
	setWishListPosition();

	/*
	 * Display cart to show that something has been added
	 */
	showCart("#shopping-cart");
	window.setTimeout("hideCart('#shopping-cart')", 2000);
};

/*
 * To create JSON object for each shopping cart item
 */
var createItemJSON = function(item) {
	var objCartItem = {};
	objCartItem["prodId"] = item.prodId;
	//objCartItem["id"] = item.id;
	objCartItem["quantity"] = item.quantity;
	objCartItem["action"] = "q";
	
	return objCartItem;
};

/*
 * To update item quantity from shopping cart
 */
var updateQuantity = function(itemProdId, updatedItemQty) {

	var itemToUpdateIndex = null;
	var itemToUpdateObj = null;

	// ToDo: to be optimized
	if ((updateCartRequestJSON != null && updateCartRequestJSON != undefined)
			&& (updateCartRequestJSON.cartItems != null && updateCartRequestJSON.cartItems != undefined)) {
		if (updateCartRequestJSON.cartItems.length > 0) {
			//itemsLabel:
			for (var i = 0; i < updateCartRequestJSON.cartItems.length; i++) {

				$.each(updateCartRequestJSON.cartItems[i], function(key, value) {
					if (key == "prodId" && value == itemProdId) {
						itemToUpdateIndex = i;
						itemToUpdateObj = updateCartRequestJSON.cartItems[i];
						// break itemsLabel;
					}
				});
			}

			itemToUpdateObj["quantity"] = updatedItemQty;
			updateCartRequestJSON.cartItems.splice(itemToUpdateIndex, 1,
					itemToUpdateObj);
			isCartUpdated = true;
		}
	}
};

/*
 * To send cart data to backend
 */
var updateCartDetails = function() {
	if (isRemoveClicked || isCartUpdated) {
		console.log("Make server call - " + JSON.stringify(updateCartRequestJSON));
		var url = "http://localhost:8080/konakart/BulkEditCartSubmit.action";
		$.ajax({
			type : 'POST',
			contentType : "application/json",
			url : url,
			data : JSON.stringify(updateCartRequestJSON),
			success : function(res) {
				console.log("Cart Response" + res);
			},
			error : function(jqXHR, textStatus, errorThrown) {
				var errorMsg = "JSON API call to the URL " + url
						+ " wasn't successful.";
				if (textStatus != null && textStatus != '') {
					errorMsg += "\nStatus:\t" + textStatus;
				}
				if (errorThrown != null && errorThrown != '') {
					errorMsg += "\nError:\t" + errorThrown;
				}
				alert(errorMsg);
			},
			dataType : 'json'
		});
		
		isRemoveClicked = false;
		isCartUpdated = false;
	}
};

/*
 * Used to update the wish list
 */
var addToWishListCallback = function(result, textStatus, jqXHR) {

	/*
	 * Go to product details page to choose options
	 */
	if (result.redirectURL != null) {
		if (result.redirectURL == "Login") {
			goToLoginPage();
		} else {
			goToProdDetailsPage(result.redirectURL);
		}		
		return;
	}

	/*
	 * Update wish list slide-out with new wlItems
	 */		
	if (result.wlItems != null && result.wlItems.length > 0) {
		txt = '<div id="wish-list-items">';
		for ( var i = 0; i < result.wlItems.length; i++) {
			var item = result.wlItems[i];
			txt += '<div class="shopping-cart-item">';
			txt += '<a href="'+ getURL("SelectProd.action", new Array("prodId",item.prodId)) +'"><img src="'+item.prodImgSrc+'" border="0" alt="'+item.prodName+'" title="'+item.prodName+'"></a>';
			txt += '<a href="'+ getURL("SelectProd.action", new Array("prodId",item.prodId)) +'" class="shopping-cart-item-title">'+item.prodName+'</a>';
			txt += getProdOptionText(item.opts, /*isWishList*/true);
			txt += '<div class="shopping-cart-item-price">';
			txt += item.formattedPrice;
			txt += '</div>';
			txt += '</div>';
		}
		txt += '</div>';
		txt += '<div id="wish-list-subtotal">';
		txt += '<div class="subtotal">';
		txt += '<div class="subtotal-label">'+result.subtotalMsg+'</div>';
		txt += '<div class="subtotal-amount">'+result.wishListTotal+'</div>';
		txt += '</div>';
		txt += '</div>';
	} else {
		txt = result.emptyWishListMsg;
	}	
	$("#wish-list-contents").html(txt);
	
	/*
	 * Update wish liat summary with new number of wlItems
	 */
	var txt = result.wishListMsg;
	if (result.numberOfItems > 0) {
		txt += " ("+result.numberOfItems+")";
	} 
	$("#wish-list").html(txt);
	
	/*
	 * Display wish liat to show that something has been added
	 */
	showWishList("#wish-list");
	window.setTimeout("hideWishList('#wish-list')", 2000);

};

/*
 * Newsletter subscription callback
 */
var subscribeNewsletterCallback = function(result, textStatus, jqXHR) {

	if (result.msg != null) {
		$("#newsletter-msg").html(result.msg);

		if (result.error==true) {
			$("#newsletter-msg").removeClass("messageStackSuccess");
			$("#newsletter-msg").addClass("messageStackError");
		} else {
			$("#newsletter-msg").removeClass("messageStackError");
			$("#newsletter-msg").addClass("messageStackSuccess");
		}
	}
};

/*
 * Callback for agree to cookies
 */
var agreeToCookiesCallback = function(result, textStatus, jqXHR) {
	 $("#cookie-container").slideUp();
};
