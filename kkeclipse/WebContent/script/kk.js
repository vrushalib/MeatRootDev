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
					// So that action class receives array values
					parmArray[i] = parmArray[i].replace("%5B","[");
					parmArray[i] = parmArray[i].replace("%5D","]");
					renderURL.setParameter(parmArray[i], parmArray[i+1]);
				}
				renderURL.setParameter("xsrf_token", document.getElementById('kk_xsrf_token').value);
			}
	        url = renderURL.toString();
	        
			$.ajax({
				type : 'POST',
				timeout : '20000',
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
function kkSearch(key, text) {

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
		// Search optimization switched off by default since can be confusing
		if (false) {
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
		} else {
			// Search based on text
			document.getElementById('searchText').value = text;
			document.getElementById('ssForm').action = getURL("QuickSearch.action");
			document.getElementById('ssForm').submit();
		}		
	}
}

/*
 * Callback for suggested search
 */
var suggestedSearchCallback = function(result, textStatus, jqXHR) {
	if (result != null && result.length > 0) {
		kkSearch(result[0].id, result[0].value);
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

function kkKeydownMobile() {
	document.getElementById('kk_key-mobile').value = "";
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
	
	// Space out menu evenly
	sizeMenu();		
	
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
	$(".add-to-cart-button")
	.click(
			function() {
				var prodId = (this.id).split('-')[1];
				callAction(new Array("prodId",prodId), 
						addToCartCallback,
						"AddToCartFromProdId.action");
				return false;
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
	 * Subscribe to newslette
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

});

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
 * Returns true if the style is currently that for small devices
 */
function isSmallStyle() {
	if($("#shopping-cart .top-bar-menu-title").is(':visible')) {
		return false;
	}
	return true;
}

/*
 * Code to display the slide out cart
 */
function showCart(cart) {
	if(!isSmallStyle()) {
		$(cart).addClass("small-rounded-corners-top shopping-cart-mouseover");
		$("#shopping-cart-container").css("display","inline");
	}
}

/*
 * Code to hide the slide out cart
 */
function hideCart(cart) {
	if(!isSmallStyle()) {
		$("#shopping-cart-container").hide();
		$(cart).removeClass("shopping-cart-mouseover small-rounded-corners-top");
	}
}

/*
 * Code to display the slide out wish list
 */
function showWishList(wishList) {
	if(!isSmallStyle()) {
		$(wishList).addClass("small-rounded-corners-top shopping-cart-mouseover");	
		$("#wish-list-container").css("display","inline");
	}
}

/*
 * Code to hide the slide out wish list
 */
function hideWishList(wishList) {
	if(!isSmallStyle()) {
		$("#wish-list-container").hide();
		$(wishList).removeClass("small-rounded-corners-top shopping-cart-mouseover");
	}
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
 * Remove "px" from the string
 */
function removepx(size){
	if (size != null && size.length > 2) {
		return parseInt(size.substring(0, size.length-2));
	} else {
		return 0;
	}
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
	/*
	 * Update cart slide-out with new basket items
	 */		
	if (result.items != null && result.items.length > 0) {
		txt = '<div id="shopping-cart-items">';
		for ( var i = 0; i < result.items.length; i++) {
			var item = result.items[i];
			txt += '<div class="shopping-cart-item">';
			txt += '<a href="'+ getURL("SelectProd.action", new Array("prodId",item.prodId)) +'"><img src="'+item.prodImgSrc+'" border="0" alt="'+item.prodName+'" title="'+item.prodName+'"></a>';
			txt += '<a href="'+ getURL("SelectProd.action", new Array("prodId",item.prodId)) +'" class="shopping-cart-item-title">'+item.prodName+'</a>';
			txt += getProdOptionText(item.opts, /*isWishList*/false);
			txt += '<div class="shopping-cart-item-price">';
			txt += item.formattedPrice;
			txt += ' '+result.quantityMsg+': '+item.quantity;
			txt += '</div>';
			txt += '</div>';
		}
		txt += '</div>';
		txt += '<div id="subtotal-and-checkout">';
		txt += '<div class="subtotal">';
		txt += '<div class="subtotal-label">'+result.subtotalMsg+'</div>';
		txt += '<div class="subtotal-amount">'+result.basketTotal+'</div>';
		txt += '<div id="shopping-cart-checkout-button" class="button small-rounded-corners">'+result.checkoutMsg+'</div>';
		txt += '</div>';
		txt += '</div>';
	} else {
		txt = result.emptyCartMsg;
	}
	$("#shopping-cart-contents").html(txt);
	
	/*
	 * Set event code on checkout button
	 */
	$("#shopping-cart-checkout-button").click(goToCheckoutPage);
	
	/*
	 * Update cart summary with new basket data
	 */
	txt1 = result.shoppingCartMsg;
	txt2 = '';

	if (result.numberOfItems > 0) {
		txt1 += '<span id="basket-items"> ('+result.numberOfItems+')</span>';
		txt2 += '<span id="basket-items">('+result.numberOfItems+')</span>';
	} 

	$("#shopping-cart .top-bar-menu-title").html(txt1); // Shopping cart link on big displays
	$("#shopping-cart .top-bar-menu-icon").html(txt2); // Shopping cart link on small displays

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

	txt1 = result.wishListMsg;
	txt2 = '';

	if (result.numberOfItems > 0) {
		txt1 += '<span id="basket-items"> ('+result.numberOfItems+')</span>';
		txt2 += '<span id="basket-items">('+result.numberOfItems+')</span>';
	} 

	$("#wish-list .top-bar-menu-title").html(txt1); // Shopping cart link on big displays
	$("#wish-list .top-bar-menu-icon").html(txt2); // Shopping cart link on small displays
	
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

/*
 * Menu sizing algorithm on each browser width change
 */

$(window).resize(function() {
	sizeMenu();
});	
	
function sizeMenu() {

	var width = $("#main-menu").width() - 2;

	// reset width and unwrap items from extra divs
	// calculate menuLineWidth
	var menuLineWidth = 0
	var numItems = 0;
	var itemPadding = 14;
	var itemMarginRight = 5;
	$("#main-menu a").each(function(index) {
		var item = $(this);
		item.css('width', 'auto');
		item.css('margin-right', itemMarginRight + 'px');
		var widthPlusPadding = item.width() + itemPadding;
		item.width(widthPlusPadding);
		menuLineWidth += widthPlusPadding + itemMarginRight;
		numItems++;
		var parent = item.parent();
		if (parent.hasClass('menu-line')) {
			item.unwrap();
		}
	});

	// Adjust for last item
	menuLineWidth -= itemMarginRight;

	if (numItems == 0) {
		return;
	}

	var numLines = Math.ceil(menuLineWidth / width);
	var itemsPerLine = Math.ceil(numItems / numLines);

	// Create arrays of items and widths for each line
	var total = 0;
	var lineIndex = 0;
	var itemCount = 0;
	var itemArray = new Array();
	var lineArray = new Array();
	var widthArray = new Array();
	$("#main-menu a").each(function(index) {
		var item = $(this);
		var w = item.width() + itemMarginRight;
		itemCount++;

		if (total + w - itemMarginRight > width || itemCount > itemsPerLine) {
			total -= itemMarginRight;
			widthArray[itemArray.length] = total;
			total = w;
			itemArray[itemArray.length] = lineArray;
			lineArray = new Array();
			lineIndex = 0;
			lineArray[lineIndex++] = item;
			itemCount = 1;
		} else {
			total += w;
			lineArray[lineIndex++] = item;
		}
	});
	if (lineArray.length > 0) {
		total -= itemMarginRight;
		widthArray[itemArray.length] = total;
		itemArray[itemArray.length] = lineArray;
	}

	// Surround each line with a div
	var index = 0;
	for ( var i = 0; i < itemArray.length; i++) {
		lineArray = itemArray[i];
		$("#main-menu a").slice(index, index + lineArray.length).wrapAll(
				'<div class="menu-line"></div>');
		index = index + lineArray.length;
	}

	// Pad lines out to same width
	for ( var i = 0; i < itemArray.length; i++) {
		lineArray = itemArray[i];
		var totalExtra = width - widthArray[i];
		var singleExtra = Math.floor((totalExtra / itemArray[i].length));
		var countExtra = 0;
		for ( var j = 0; j < lineArray.length; j++) {
			var widget = lineArray[j];
			if (j == lineArray.length - 1) {
				widget.css('margin-right', '0px');
				var w = widget.width();
				var extra = totalExtra - countExtra;
				widget.width(w + extra);
			} else {
				var w = widget.width();
				var extra = singleExtra;
				widget.width(w + extra);
			}
			countExtra += singleExtra;
		}
	}
}
	

/*
 * Function for setting controls of horizontal carousel 
 */
function setControls(carousel, prev, next) {
	var items = carousel.jcarousel('items');
	var visible = carousel.jcarousel('visible');
	if (items[0] == visible[0]) {
		prev.removeClass('prev-items').addClass('prev-items-inactive');
	} else {
		prev.removeClass('prev-items-inactive').addClass('prev-items');
	}
	if (items[items.length - 1] == visible[visible.length - 1]) {
		next.removeClass('next-items').addClass('next-items-inactive');
	} else {
		next.removeClass('next-items-inactive').addClass('next-items');
	}
}  



