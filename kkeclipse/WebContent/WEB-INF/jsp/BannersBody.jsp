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

<link rel="stylesheet"	href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/overcast/jquery-ui.min.css"
	type="text/css" media="all" />
<link	href="styles/uiv2_main.min.css"	rel="stylesheet" type="text/css" />
<script	src="js/jquery.cycle.js"></script>
<script src="js/uiv2_main.min.js"	type="text/javascript"></script>

<%@include file="Taglibs.jsp" %>

<%@ page import="java.util.List" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>

<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
<% com.konakart.app.EngineConfig engineConfig = new com.konakart.app.EngineConfig(); %>
<% com.konakart.appif.KKEngIf engine = new com.konakart.app.KKEng(engineConfig); %>
<% boolean hideRow1 =  kkEng.getPropertyAsBoolean("main.page.hide.banner.row1", false);%>
<% boolean hideRow2 =  kkEng.getPropertyAsBoolean("main.page.hide.banner.row2", false);%>

<%
	if (!hideRow1) {
%>

<div id="slideshow" class="rounded-corners">
		<div id="uiv2-slideshow">
			<div class="uiv2-slides">
				<ul style="width: 940px; height: 255px;">
	<% for (int i = 0; i <catMgr.getCats().length; i++) {%>
	    <%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i];	%>  
	    <%String menuClass; %>
	    <%if (i == catMgr.getCats().length-1){ %>
	        <% menuClass = "menu-item rounded-corners last-child"; %>
	    <% } else { %>
	        <% menuClass = "menu-item rounded-corners"; %>
	    <% } %>
	    <%if(cat.getName().contains("Fresh")) {%>
					<li style="width:940px; height: 255px;" id="uiv2-slide-one"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/fresh.jpg') center left no-repeat; background-size: 100% 100%;">
								<div class="button2"><span>Tender, healthier and tastier<br/> meat straight from the farms!</span><a href='<%="SelectCat.action?catId="+cat.getId()%>'><input type="button" class="button3" value="Explore" /></a> </div>
								</div></li>
		<%}else if(cat.getName().contains("Frozen")){ %>						
					<li style="width: 940px; height: 255px;" id="uiv2-slide-two"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/frozen.jpg') center left no-repeat; background-size: 100% 100%;">
								<div class="button2"><span>Why wait for a season<br/>frozen gives the  same freshness <br/> throughout the year!!</span><a href='<%="SelectCat.action?catId="+cat.getId()%>'><input type="button" class="button3" value="Explore" /></a> </div></div></li>
			    <%}else if(cat.getName().contains("Processed")){ %>				
					<li style="width: 940px; height: 255px;" id="uiv2-slide-three"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/processed.jpg') center left no-repeat; background-size: 100% 100%;">
								<div class="button2"><span>From starters to mains,<br/> prepare anything within no time.</span><a href='<%="SelectCat.action?catId="+cat.getId()%>'><input type="button" class="button3" value="Explore" /></a> </div>
								</div></li>
		<% } %>
	<% } %>
					<li style="width: 940px; height: 255px;" id="uiv2-slide-four">
								<div class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/DeliveryArea.png') center left no-repeat; background-size: 100% 100%;">
								<div class="button2" id="pincode_area"><input type="text" id="pincode" class="deliveryarea" placeholder="Add your Pincode to help us scale" /><input type="button" class="gobutton" value="Go" id="go"/> </div>
								<div class="button2" id="email_area" style="display:none;"><input type="text" id="emailId" class="deliveryarea" placeholder="Please help us with your email" /><input type="button" class="gobutton" value="Done" id="done"/> </div>
								<div class="button2" id="message_area" style="display:none;"><span class="deliveryarea" id="success_message" style="display:none;">Thank you for your valuable input</span><span class="deliveryarea" id="error_message" style="display:none;">Please enter valid pincode/emailId</span><input type="button" class="gobutton" value="Back" id="back"/> </div>
								</div></li>
								
				</ul>
			</div>
		
	     <ul class="uiv2-slides-nav">
			<li class="on
            "><span class="arrow  caption-four"></span><a
				href="#uiv2-slide-one"></a> <span>Fresh</span></li>
			<li class="
            "><span class="arrow  caption-four"></span><a
				href="#uiv2-slide-two"></a><span>Frozen</span></li>
			<li class="
            "><span class="arrow  caption-four"></span><a
				href="#uiv2-slide-three"></a><span>Processed</span></li>

			<li class=""><span class="arrow  caption-four"></span><a
					href="#uiv2-slide-four"></a><span>Delivery Area</span></li>
			</ul>
		</div>
	</div>
	
<%
	}
%>






