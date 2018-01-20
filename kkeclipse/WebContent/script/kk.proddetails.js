$(function() {
	
	// Accordion
	var prodMobile = $("#product-content-mobile");
	if (prodMobile !=null && prodMobile.attr("class") != null && prodMobile.attr("class").indexOf("accordion-show-reviews") == 0) {
		prodMobile.accordion({
			collapsible: true,
			active: 2,
			heightStyle: "content"
		});
	} else {
		prodMobile.accordion({
			collapsible: true,
			active: 0,
			heightStyle: "content"
		});
		$(window).scrollTop(0);
	}
	
	if ($("#product-reviews-tab").length) {

		$(window).scroll(function() {
			 $.cookie('y_cookie', $(window).scrollTop(), { expires: 7, path: '/' });
		});
		
		var y = $.cookie('y_cookie');
		if (y != null && y.length > 0) {
			$(window).scrollTop(y);
		}
		
		$("#AddToCartForm").submit(function(){
			var formInput=$(this).serialize();	
			addToCartFormPost(formInput);
			return false;					
		});
		
		$("#AddToCartFormSmall").submit(function(){
			var formInput=$(this).serialize();	
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
		var imgBase = document.getElementById('gallery_nav_base').value
					  +document.getElementById('gallery_nav_dir').value
					  +document.getElementById('gallery_nav_uuid').value;
		var extension = document.getElementById('gallery_nav_extension').value;
		
		$("#gallery_nav").empty();
		$("#gallery_output").empty();
		var index = 1;		
		getImage(imgBase, index, extension);
	}	
});

// Common Code
function addToCartFormPost(formInput){
	
	if (document.getElementById('kk_portlet_id')) {
		var postArray = new Array();
		var parmArray = formInput.split('&');
		var j=0;
		for ( var i = 0; i < parmArray.length; i++) {
			var parms = parmArray[i];
			var parmsArray = parms.split("=");
			postArray[j++] = parmsArray[0];
			postArray[j++] = parmsArray[1];
		}
		if (document.getElementById('addToWishList').value=="true") {
			if (document.getElementById('wishListId').value!="-1") {
				callAction(postArray, addToGiftRegistryCallback, 'AddToCartOrWishListFromPost.action');
			} else {
				callAction(postArray, addToWishListCallback, 'AddToCartOrWishListFromPost.action');
			} 		
		} else {
	 		callAction(postArray, addToCartCallback, 'AddToCartOrWishListFromPost.action');
		}
	} else {			
		if (document.getElementById('addToWishList').value=="true") {
			if (document.getElementById('wishListId').value!="-1") {
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
	if (items[items.length-1] == visible[visible.length-1]) {
		next.removeClass('next-items-up').addClass('next-items-up-inactive');
	} else {
		next.removeClass('next-items-up-inactive').addClass('next-items-up');
	}
}


function getImage(imgBase, index, extension) {
	var imgSrcSmall = imgBase + "_" + index + "_small"+extension;
	var imgSrcLarge = imgBase + "_" + index + "_big"+extension;

	var img = new Image();
	img.onload = function() {
		var smallImg = '<a rel="img' + index
		+ '" href="javascript:;"><img src="' + imgSrcSmall
		+ '"/></a>';
		$("#gallery_nav").append(smallImg);
		var largeImg = '<img id="img' + index + '" src="' + imgSrcLarge + '"/>';
		$("#gallery_output").append(largeImg);
		$("#gallery_output img").not(":first").hide();
		index++;
		getImage(imgBase, index, extension);
	};
	img.onerror = function() {
		finaliseImageGallery();		
	}; 
	img.src = imgSrcSmall;
}

function finaliseImageGallery() {	
	$("#gallery_output img").eq(0).addpowerzoom();
	$("#gallery a").click(function() {
		var id = "#" + this.rel;
		if ($(id).is(":hidden")) {
			$("#gallery_output img").slideUp();
			$(id).slideDown( function() {
				$(id).addpowerzoom();
			});
		}
	});	
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
function relatedCarousel_nextCallback(carousel,control,flag) {
    if (flag) {
    	jQuery('#kk-up-rc').addClass("next-items-up").removeClass("next-items-up-inactive");
	} else {
    	jQuery('#kk-up-rc').addClass("next-items-up-inactive").removeClass("next-items-up");
		
	}
};

// Down
function relatedCarousel_prevCallback(carousel,control,flag) {
    if (flag) {
    	jQuery('#kk-down-rc').addClass("previous-items-down").removeClass("previous-items-down-inactive");
	} else {
    	jQuery('#kk-down-rc').addClass("previous-items-down-inactive").removeClass("previous-items-down");
		
	}
};

//Carousel init
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
function alsoBought_nextCallback(carousel,control,flag) {
    if (flag) {
    	jQuery('#kk-up-ab').addClass("next-items-up").removeClass("next-items-up-inactive");
	} else {
    	jQuery('#kk-up-ab').addClass("next-items-up-inactive").removeClass("next-items-up");
		
	}
};

// Down
function alsoBought_prevCallback(carousel,control,flag) {
    if (flag) {
    	jQuery('#kk-down-ab').addClass("previous-items-down").removeClass("previous-items-down-inactive");
	} else {
    	jQuery('#kk-down-ab').addClass("previous-items-down-inactive").removeClass("previous-items-down");
		
	}
};

function setAddToWishList() {
	$(".addToWishList").val("true");
	$(".wishListId").val("-1");
			//document.getElementById('addToWishList').value="true";
			//document.getElementById('wishListId').value="-1";
		}
	
function resetAddToWishList() {
	$(".addToWishList").val("false");	    
	//document.getElementById('addToWishList').value="false";
		}

function setWishListId(id) {
	$(".addToWishList").val("true");
	$(".wishListId").val(id);

	//document.getElementById('wishListId').value=id;
	//document.getElementById('addToWishList').value="true";
}

		
function addtoCartOrWishListFunc(formId){
	// Random value needed to stop IE thinking that it doesn't have to send the request again because it hasn't changed
	var d = new Date();
	document.getElementById('random').value=d.getMilliseconds();
	var val = $("#"+formId).validate({
		errorPlacement: function(error, element) {
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
		$("#"+formId).submit();
	}
	return val;	
}

function imgExists(imgPath) {
	 var http = jQuery.ajax({
		    type:"HEAD",
		    url: imgPath,
		    async: false
		  });
		  return http.status!=404;			
	 }

/*
 * Used to update the wish list
 */
var addToGiftRegistryCallback = function(result, textStatus, jqXHR) {	
	var id = document.getElementById('wishListId').value;
	return redirect(getURL("ShowWishListItems.action", new Array("wishListId",id)));
};



		
