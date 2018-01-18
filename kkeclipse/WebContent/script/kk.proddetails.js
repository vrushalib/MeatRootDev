$(function() {

	// Accordion
	var prodMobile = $("#product-content-mobile");
	if (prodMobile != null && prodMobile.attr("class") != null && prodMobile.attr("class").indexOf("accordion-show-reviews") == 0) {
		prodMobile.accordion({
			collapsible : true,
			active : 2,
			heightStyle : "content"
		});
	} else {
		prodMobile.accordion({
			collapsible : true,
			active : 0,
			heightStyle : "content"
		});
		$(window).scrollTop(0);
	}

	if ($("#product-reviews-tab").length) {

		$(window).scroll(function() {
			$.cookie('y_cookie', $(window).scrollTop(), {
				expires : 7,
				path : '/'
			});
		});

		var y = $.cookie('y_cookie');
		if (y != null && y.length > 0) {
			$(window).scrollTop(y);
		}

		$("#AddToCartForm").submit(function() {
			var formInput = $(this).serialize();
			addToCartFormPost(formInput);
			return false;
		});

		$("#AddToCartFormSmall").submit(function() {
			var formInput = $(this).serialize();
			addToCartFormPost(formInput);
			return false;
		});

		// Tabs
		if ($("#product-reviews-tab").attr("class").indexOf("selected-product-content-tab") >= 0) {
			$("#product-description").hide();
		} else {
			$("#product-reviews").hide();
			$(window).scrollTop(0);
		}
		$("#product-specifications").hide();

		$("#product-reviews-tab").click(function() {
			$("#product-description-tab").removeClass("selected-product-content-tab");
			$("#product-specifications-tab").removeClass("selected-product-content-tab");
			$("#product-reviews-tab").addClass("selected-product-content-tab");
			$("#product-description").hide();
			$("#product-specifications").hide();
			$("#product-reviews").show();
		});

		$("#product-specifications-tab").click(function() {
			$("#product-description-tab").removeClass("selected-product-content-tab");
			$("#product-specifications-tab").addClass("selected-product-content-tab");
			$("#product-reviews-tab").removeClass("selected-product-content-tab");
			$("#product-description").hide();
			$("#product-specifications").show();
			$("#product-reviews").hide();
		});

		$("#product-description-tab").click(function() {
			$("#product-description-tab").addClass("selected-product-content-tab");
			$("#product-specifications-tab").removeClass("selected-product-content-tab");
			$("#product-reviews-tab").removeClass("selected-product-content-tab");
			$("#product-description").show();
			$("#product-specifications").hide();
			$("#product-reviews").hide();
		});

		// Images
		getImage("");
	}
});


function addToCartFormPost(formInput) {

	if (document.getElementById('kk_portlet_id')) {
		var postArray = new Array();
		var parmArray = formInput.split('&');
		var j = 0;
		for (var i = 0; i < parmArray.length; i++) {
			var parms = parmArray[i];
			var parmsArray = parms.split("=");
			postArray[j++] = parmsArray[0];
			postArray[j++] = parmsArray[1];
		}
		if (document.getElementById('addToWishList').value == "true") {
			if (document.getElementById('wishListId').value != "-1") {
				callAction(postArray, addToGiftRegistryCallback, 'AddToCartOrWishListFromPost.action');
			} else {
				callAction(postArray, addToWishListCallback, 'AddToCartOrWishListFromPost.action');
			}
		} else {
			callAction(postArray, addToCartCallback, 'AddToCartOrWishListFromPost.action');
		}
	} else {
		if (document.getElementById('addToWishList').value == "true") {
			if (document.getElementById('wishListId').value != "-1") {
				$.getJSON('AddToCartOrWishListFromPost.action', formInput, addToGiftRegistryCallback);
			} else {
				$.getJSON('AddToCartOrWishListFromPost.action', formInput, addToWishListCallback);
			}
		} else {
			$.getJSON('AddToCartOrWishListFromPost.action', formInput, addToCartCallback);
		}
	}
}

function setVerticalControls(carousel, prev, next) {
	var items = carousel.jcarousel('items');
	var visible = carousel.jcarousel('visible');
	if (items[0] == visible[0]) {
		prev.removeClass('prev-items-down').addClass('prev-items-down-inactive');
	} else {
		prev.removeClass('prev-items-down-inactive').addClass('prev-items-down');
	}
	if (items[items.length - 1] == visible[visible.length - 1]) {
		next.removeClass('next-items-up').addClass('next-items-up-inactive');
	} else {
		next.removeClass('next-items-up-inactive').addClass('next-items-up');
	}
}


function getImage(combinedCode) {
	
	var base =  document.getElementById('gallery_nav_base').value;
	var uuid = document.getElementById('gallery_nav_uuid').value;
	var imgNamesStr = document.getElementById('gallery_img_names').value;	
	var imgNames = imgNamesStr.split(",");
	
	var smacount = 0;
	var smallRegExp =  new RegExp("^.*" + uuid + "_" + combinedCode + "\\d+" + "_small" + ".*$");
	var bigRegExp =  new RegExp("^.*" + uuid + "_" + combinedCode + "\\d+" + "_big" + ".*$");
	var smallNames = [];
	var bigNames = [];
	var numImgs = 0;
	for (var i = 0; i < imgNames.length; i++) {
		var name = imgNames[i];
		if (smallRegExp.test(name)) {
			var num = getImgNumberFromName(name);
			smallNames[num] = name;
		}else if (bigRegExp.test(name)) {
			var num = getImgNumberFromName(name);
			bigNames[num] = name;
			numImgs++;
		}
	}
	
	if (smallNames.length == 0 || bigNames.length == 0) {
		getImage("");
		return;
	}
	
	var processed = 0;
	var galleryNav = $("#gallery_nav");
	var galleryOut = $("#gallery_output");
	for (var i = 0; i < 50; i++) {
		var smallImg = smallNames[i];
		var bigImg = bigNames[i];
		if (smallImg != null && bigImg != null) {
			galleryNav.append('<a rel="img' + i + '" href="javascript:;"><img src="' + base + smallImg + '"/></a>');
			galleryOut.append('<img id="img' + i + '" src="' + base + bigImg + '"/>');
			if (processed++ == 0) {
				/* Remove all except one we've just added. In this way the widget
				 * doesn't collapse like it would if emptied before adding new
				 * image.
				 */
				$("#gallery_output img").not(':last').remove();
				$("#gallery_nav img").not(':last').remove();		
			}
			$("#gallery_output img").not(":first").hide();
		}
		if (numImgs == processed) {
			break;
		}	
	}
	finaliseImageGallery();	
};


function finaliseImageGallery() {
	$("#gallery_output img").eq(0).addpowerzoom();
	$("#gallery a").click(function() {
		var id = "#" + this.rel;
		if ($(id).is(":hidden")) {
			$("#gallery_output img").slideUp();
			$(id).slideDown(function() {
				$(id).addpowerzoom();
			});
		}
	});
}

/**
 * Get the image number from the image name
 */
function getImgNumberFromName(imgName) {
	var end = imgName.lastIndexOf('_');
	var start = imgName.lastIndexOf('_', end - 1);
	var imgNum = imgName.substring(start + 1, end);
	return imgNum;
};

// Reload images if they change based on the option
function optionChanged(opt) {
	var combinedCode = opt.options[opt.selectedIndex].id;
	if (combinedCode.length > 0) {
		getImage(combinedCode);
	}
}

// Carousel init
function relatedCarousel_initCallback(carousel) {

	jQuery('#kk-up-rc').bind('click', function() {
		carousel.next();
		return false;
	});

	jQuery('#kk-down-rc').bind('click', function() {
		carousel.prev();
		return false;
	});
};

// Up
function relatedCarousel_nextCallback(carousel, control, flag) {
	if (flag) {
		jQuery('#kk-up-rc').addClass("next-items-up").removeClass("next-items-up-inactive");
	} else {
		jQuery('#kk-up-rc').addClass("next-items-up-inactive").removeClass("next-items-up");

	}
};

// Down
function relatedCarousel_prevCallback(carousel, control, flag) {
	if (flag) {
		jQuery('#kk-down-rc').addClass("previous-items-down").removeClass("previous-items-down-inactive");
	} else {
		jQuery('#kk-down-rc').addClass("previous-items-down-inactive").removeClass("previous-items-down");

	}
};

// Carousel init
function alsoBought_initCallback(carousel) {

	jQuery('#kk-up-ab').bind('click', function() {
		carousel.next();
		return false;
	});

	jQuery('#kk-down-ab').bind('click', function() {
		carousel.prev();
		return false;
	});
};

// Up
function alsoBought_nextCallback(carousel, control, flag) {
	if (flag) {
		jQuery('#kk-up-ab').addClass("next-items-up").removeClass("next-items-up-inactive");
	} else {
		jQuery('#kk-up-ab').addClass("next-items-up-inactive").removeClass("next-items-up");

	}
};

// Down
function alsoBought_prevCallback(carousel, control, flag) {
	if (flag) {
		jQuery('#kk-down-ab').addClass("previous-items-down").removeClass("previous-items-down-inactive");
	} else {
		jQuery('#kk-down-ab').addClass("previous-items-down-inactive").removeClass("previous-items-down");

	}
};

function setAddToWishList() {
	$(".addToWishList").val("true");
	$(".wishListId").val("-1");
	// document.getElementById('addToWishList').value="true";
	// document.getElementById('wishListId').value="-1";
}

function resetAddToWishList() {
	$(".addToWishList").val("false");
	// document.getElementById('addToWishList').value="false";
}

function setWishListId(id) {
	$(".addToWishList").val("true");
	$(".wishListId").val(id);

	// document.getElementById('wishListId').value=id;
	// document.getElementById('addToWishList').value="true";
}

function addtoCartOrWishListFunc(formId) {
	// Random value needed to stop IE thinking that it doesn't have to send the
	// request again because it hasn't changed
	var d = new Date();
	document.getElementById('random').value = d.getMilliseconds();
	var val = $("#" + formId).validate({
		errorPlacement : function(error, element) {
			var val = error[0].innerHTML;
			if (val.length > 0) {
				var msgElement = element.parent().children(".validation-msg");
				if (msgElement != null) {
					error.appendTo(msgElement);
				}
			}
		}
	}).form();
	if (val) {
		$("#" + formId).submit();
	}
	return val;
}

function imgExists(imgPath) {
	var http = jQuery.ajax({
		type : "HEAD",
		url : imgPath,
		async : false
	});
	return http.status != 404;
}

/*
 * Used to update the wish list
 */
var addToGiftRegistryCallback = function(result, textStatus, jqXHR) {
	var id = document.getElementById('wishListId').value;
	return redirect(getURL("ShowWishListItems.action", new Array("wishListId", id)));
};
