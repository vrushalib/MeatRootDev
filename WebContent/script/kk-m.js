/*
 * Sends an AJAX request to a struts action
 */
function callAction(parmArray, callback, url) {
	
	var xsrfToken = document.getElementById('kk_xsrf_token').value;
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
				renderURL.setParameter("xsrf_token", xsrf_token);
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
		var parms;
        if (parmArray) {
        	parms = '{';
			for ( var i = 0; i < parmArray.length; i=i+2) {
				parms = parms + '"' + parmArray[i]+'":"'+ parmArray[i+1]+ '"';
				if (i+2 < parmArray.length) {
					parms = parms + ',';
				}
			}
			parms = parms + ',"xsrf_token":"'+ xsrfToken + '"';
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
	return url;
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

function redirect(action) {
	var base = $('#kk_base').attr('href');
	var redirectUrl = base+action;
	window.location = redirectUrl;
	return false;
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
 * Used by address maintenance panels
 */
function changeCountry() {
	if (document.getElementById('state')) {
		document.getElementById('state').value="";
	}	
	document.getElementById('countryChange').value="1";
	document.getElementById('form1').submit();
}