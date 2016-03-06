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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>

 <%if (kkEng != null) {%>
 	<tiles:useAttribute id="contentClass" name="contentClass" classname="java.lang.String" ignore="true"/>
    <%kkEng.setContentClass(contentClass);%>
    <% if (kkEng.getProductMgr().getCurrentCategoriesLength() == 0 && contentClass.equalsIgnoreCase("narrow")){%>
    	<%kkEng.setContentClass("wide");%>
    <% } %>
    
    <tiles:useAttribute id="defName" name="defName" classname="java.lang.String" ignore="true"/>
    
    <%if (kkEng.isPortlet()) {%>
	    <%request.getSession().setAttribute("konakartKey", kkEng); %> 
	    <% boolean dontSetContext =  kkEng.getPropertyAsBoolean("dont.set.portlet.context.path", false);%>
	    <%if (!dontSetContext) {%>
		    <%kkEng.setPortletContextPath(request.getContextPath());%>
		    <input id="kk_context" type="hidden" value="<%=request.getContextPath()%>"/>
	    <% } %>
		<input id="kk_portlet_id" type="hidden" value="<%=request.getAttribute("PORTLET_ID")%>"/>
		<input id="kk_sample_url" type="hidden" value="<s:url action='KK_ACTION'/>"/>	
		<input type="hidden" value="<%=kkEng.getXsrfToken()%>" id="kk_xsrf_token"/>			
		<div id="kk-portlet-body">		
    <% } else { %>
		<html>
			<head>
				<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=1" />
				<meta name="google-site-verification" content="tckwGFjiZX1gylwtvH5QWdJ-y0B7sF8nMmiT4yi61FQ" />
				<title>MeatRoot : Online Meat Shop - Buy Fresh, Frozen and Processed Chicken, Mutton and Fish online</title>
				<link rel="icon" href="images/titlelogo.jpg" type="image/x-icon">
				<meta name="keywords" content="<%=kkEng.getMetaKeywords()%>" />
				<meta name="description" content="<%=kkEng.getMetaDescription()%>" />
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				<link type="text/css" rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/themes/smoothness/jquery-ui.css" />
				<link type="text/css" rel="stylesheet" href="<%=kkEng.getStyleBase()%>/jquery.selectboxit.css" />
				<link type="text/css" rel="stylesheet" href="<%=kkEng.getStyleBase()%>/font-awesome/css/font-awesome.css" />
				<link type='text/css' rel='stylesheet' href='http://fonts.googleapis.com/css?family=Open+Sans:400italic,600italic,400,600,700'  >		
				<link type="text/css" rel="stylesheet" href="<%=kkEng.getStyleBase()%>/jcarousel.css" />				
				<link type="text/css" rel="stylesheet" href="<%=kkEng.getStyleBase()%>/kk-style.css" />

		 		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script> 
				<script type="text/javascript" src="http://code.jquery.com/jquery-migrate-1.2.1.min.js"></script>				
				<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.0/jquery-ui.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.validate.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.json-2.3.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.tools.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.selectboxit.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.ui.datepicker-en.js"></script>			 
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.ui.datepicker-de.js"></script>			 
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.ui.datepicker-pt.js"></script>			 
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.ui.datepicker-es.js"></script>			 
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/ddpowerzoomer.js">
				/***********************************************
				* Image Power Zoomer- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
				* This notice MUST stay intact for legal use
				* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
				***********************************************/
				</script>			 
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.jcarousel.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.touchSwipe.min.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/jquery.cookie.js"></script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/kk.js"></script>	
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/kk.validation.js"></script>	
				<script>
				// Picture element HTML5 shiv
				document.createElement( "picture" );
				</script>
				<script type="text/javascript" src="<%=kkEng.getScriptBase()%>/picturefill-min.js"></script>							
				<%
				// Set the base
				String scheme = pageContext.getRequest().getScheme()+"://";
				String server = pageContext.getRequest().getServerName();
				int port = pageContext.getRequest().getServerPort();
				String path = session.getServletContext().getContextPath();
				String base = scheme+server+((port==80)?"":":"+port)+path+"/";
				%>
				<base href="<%=base%>"/>										
				<!--- KonaKart v7.4.0.1.12158 -->
				
				<script type="text/javascript">
				$(window).bind("load", function(){
					$("#page-container").css("overflow","hidden");
				});
				
				 (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
					  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
					  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
					  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

					  ga('create', 'UA-55023986-1', 'auto');
					  ga('send', 'pageview');
					 /* 
					  (function() {
					      var a = document.createElement('script');a.type = 'text/javascript'; a.async = true;
					      a.src=('https:'==document.location.protocol?'https://':'http://cdn.')+'chuknu.sokrati.com/15047/tracker.js';
					      var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(a, s);
					           })();
					*/

				</script>
			</head>
			<%if (defName != null) {%>
				<body id="<%=defName%>">
			<%} else {%>
				<body>
			<%}%>
			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" id="kk_xsrf_token"/>
	<%}%>
	<script language="JavaScript" type="text/javascript">
		if (top.location != location) {
					top.location.href = document.location.href ;
			}
				
			// Set jquery validation messages
			jQuery.validator.messages = {
					state: '<%=kkEng.getMsg("jquery.validator.required")%>',
					country: '<%=kkEng.getMsg("jquery.validator.required")%>',
					required: '<%=kkEng.getMsg("jquery.validator.required")%>',
					creditcard: '<%=kkEng.getMsg("jquery.validator.creditcard")%>',
					digits: '<%=kkEng.getMsg("jquery.validator.digits")%>',
					maxlength: jQuery.validator.format('<%=kkEng.getMsg("jquery.validator.maxlength")%> {0} <%=kkEng.getMsg("jquery.validator.maxlength1")%>'),
					minlength: jQuery.validator.format('<%=kkEng.getMsg("jquery.validator.minlength")%> {0} <%=kkEng.getMsg("jquery.validator.minlength1")%>'),
					expirydate: '<%=kkEng.getMsg("jquery.validator.expiryMMYY")%>',
					email:	'<%=kkEng.getMsg("jquery.validator.email")%>',
					url:	'<%=kkEng.getMsg("jquery.validator.url")%>',
					number:	'<%=kkEng.getMsg("jquery.validator.number")%>',
 					equalTo: '<%=kkEng.getMsg("jquery.validator.equalTo")%>'
		};		 
	</script>			
	
				<tiles:insertAttribute name="header1" />
				<tiles:insertAttribute name="header2" />
	    		<div id="page-container">
	    			<div id="page" class="<tiles:insertAttribute name="pageClass"/>">
						<tiles:insertAttribute name="header3" />
						<tiles:insertAttribute name="header4" />
						<tiles:insertAttribute name="left1" />
						<div id="content" class="<%=kkEng.getContentClass()%>">
							<tiles:insertAttribute name="body1" /> 
					 		<tiles:insertAttribute name="body2" /> 
							<tiles:insertAttribute name="body3" />
							<tiles:insertAttribute name="body4" />
							<tiles:insertAttribute name="body5" />
							<tiles:insertAttribute name="body6" />
				    	</div>
						<tiles:insertAttribute name="footer1" />
		    		</div>
	    		</div>
				<tiles:insertAttribute name="footer2" />
						
				<%=kkEng.getAnalyticsCode()%>
    <%if (!kkEng.isPortlet()) {%>
     <!--Start of Tawk.to Script-->
        <script type="text/javascript">
                var $_Tawk_API={},$_Tawk_LoadStart=new Date();
                (function(){
                var s1=document.createElement("script"),s0=document.getElementsByTagName("script")[0];
                s1.async=true;
                s1.src='https://embed.tawk.to/56011a5772d58d716ae44217/default';
                s1.charset='UTF-8';
                s1.setAttribute('crossorigin','*');
                s0.parentNode.insertBefore(s1,s0);
                })();
        </script>
        <!--End of Tawk.to Script-->

	<!-- Start of Agnie Script, Google Tag Manager -->

		<noscript>
			<iframe src="//www.googletagmanager.com/ns.html?id=GTM-NV3N9M"
				height="0" width="0" style="display:none;visibility:hidden">
			</iframe>
		</noscript>
		<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
			new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
			j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
			'//www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
			})(window,document,'script','dataLayer','GTM-NV3N9M');
		</script>
		

	<!-- End of Agnie Script, Google Tag Manager -->

	<!-- ViralMint JS -->
		
	<script type="text/javascript">
    			window.vrlmnt = (function(d, s, id) {
    			var js, vjs = d.getElementsByTagName(s)[0];
    			if (d.getElementById(id)) return; js = d.createElement(s);
    			js.src = "//cdn.viralmint.com/js/viralmint-min.js";
    			js.id = id; js.acc_id = "1444756064";
    			vjs.parentNode.insertBefore(js, vjs);
    			return window.vrlmnt || (v = { _e: [], ready: function(f){v._e.push(f)}});
    		}(document, "script", "viralmint-js"));
	</script>
	
	<!-- ViralMint JS -->

	<!-- ViralMint Custom Variable -->
<!--		<script type="text/javascript">
			vrlmnt.ready(function () {
    			vrlmnt.setCustomData({
        		login_status: "TRUE_OR_FALSE",
        		total_cart_value: TOTAL_CART_AMOUNT,
        		total_purchases: NUMBER_OF_ITEM_IN_CART,
        		purchases: [{
         			id: "1ST_PRODUCT_ID",
            			title: "1ST_PRODUCT_TITLE_HERE",
            			category: "1ST_PRODUCTS_CATEGORY",
            			link: "1ST_PRODUCT_ABSOLUTE_LINK",
            			image: "1ST_PRODUCT_ABSOLUTE_IMAGE_URL",
            			price: 1ST_PRODUCT_PRICE,
            			quantity: 1ST_PRODUCT_QUANTITY
        		}, {
         			id: "2ND_PRODUCT_ID",
            			title: "2ND_PRODUCT_TITLE_HERE",
            			category: "1ST_PRODUCTS_CATEGORY",
            			link: "2ND_PRODUCT_ABSOLUTE_LINK",
            			image: "2ND_PRODUCT_ABSOLUTE_IMAGE_URL",
            			price: 2ND_PRODUCT_PRICE,
            			quantity: 2ND_PRODUCT_QUANTITY
        			}]
     			});
		});
		</script>
	-->
		<!-- ViralMint Custom Variable -->
	
	<!-- KudoBuzz Script -->
<!--	<script>
		!function(){ 
			var e=document.createElement("script");
			e.type="text/javascript",
			e.async=!0;
			var t=location.protocol+"//widgets.kudobuzz.com/js/widgetLoader.js";
			e.src=t;document.getElementsByTagName("head")[0].appendChild(e);
			window.Kudos={Widget:function(e){this.uid=e.uid}},
			Kudos.Widget({uid:"44a4y2t24323p274r2b4s2330314y2z274v2y2a4y2r2q2w2t2c4w2"})
		}()
	</script>
-->
	<!-- End of KudoBuzz -->


	<!-- YotPo Script -->
<!--
		<script type="text/javascript">
			(function e(){var e=document.createElement("script");e.type="text/javascript",e.async=true,e.src="//staticw2.yotpo.com/qf0SjfLBl29nxy9XVWNdNalL52dsXzscAhknL8Ce/widget.js";var t=document.getElementsByTagName("script")[0];t.parentNode.insertBefore(e,t)})();
		</script>
                        
-->
	<!-- End of YotPo -->

<script>(function(e){var t,n,r,i,s,o,u,a,f,l,c,h,p,d,v;z="script";l="refcandy-purchase-js";c="refcandy-mint";p="go.referralcandy.com/purchase/";t="data-app-id";r={email:"a",fname:"b",lname:"c",amount:"d",currency:"e","accepts-marketing":"f",timestamp:"g","referral-code":"h",locale:"i","external-reference-id":"k",signature:"ab"};i=e.getElementsByTagName(z)[0];s=function(e,t){if(t){return""+e+"="+encodeURIComponent(t)}else{return""}};d=function(e){return""+p+h.getAttribute(t)+".js?aa=75&"};if(!e.getElementById(l)){h=e.getElementById(c);if(h){o=e.createElement(z);o.id=l;a=function(){var e;e=[];for(n in r){u=r[n];v=h.getAttribute("data-"+n);e.push(s(u,v))}return e}();o.src=""+e.location.protocol+"//"+d(h.getAttribute(t))+a.join("&");return i.parentNode.insertBefore(o,i)}}})(document);</script>


	</body>
	</html>
	<%} else {%>
		</div>
	<%}%>
<%}%>
