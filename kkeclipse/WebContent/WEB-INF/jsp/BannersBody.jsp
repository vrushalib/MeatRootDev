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

<script type="text/javascript">

$(document).ready(function(){
	$('#go').click(function(){
		var pincode = $("#go").val();
		$("#go").hide();
		$("#done").show();
		$("#pincode").append(pincode);
	});
});
</script>

<!-- <script type="text/javascript">
    // Space out menu evenly
	$(function() {
		var total=0;
		var itemArray = new Array();
		$("#main-menuBanner a").each(function(index){
			var margin = $(this).css("margin-right");
			var marginInt = parseInt(margin.substring(0, margin.length-2)); // remove px
			total += ($(this).width()+marginInt);
			itemArray[index]=$(this).width();
		});		
		var width =  $("#page").css("width");
		var widthInt = parseInt(width.substring(0, width.length-2)); // remove px	
		var extra = widthInt-total;
		extra = Math.floor((extra / itemArray.length));
		$("#main-menuBanner a").each(function(index){$(this).width(itemArray[index]+extra);});		
	});				
</script> -->






<%
	if (!hideRow1) {
%>
<div id="slideshow" class="rounded-corners">
	<%-- <%
		if ((int) (Math.random() * 100) > 50) {
	%>
	<a href="SelectProd.action?prodId=34"><div id="slide-1"
			class="slide rounded-corners"
			style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/kindle-fire-hd.jpg');"></div></a>
	<%
		} else {
	%>
	<a href="SelectProd.action?prodId=33"><div id="slide-1"
			class="slide rounded-corners"
			style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/delonghi.jpg');"></div></a>
	<%
		}
	%> --%>
	
	 
 
	
	
		<div id="uiv2-slideshow">
			<div class="uiv2-slides">
				<ul style="width: 940px; height: 255px;">
	<% 	for (int i = 0; i <catMgr.getCats().length; i++) {%>
	<%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i];	%>  

	<%String menuClass; %>
	
	<%if (i == catMgr.getCats().length-1){ %>
	<% menuClass = "menu-item rounded-corners last-child"; %>
	<% } else { %>
	<% menuClass = "menu-item rounded-corners"; %>
	<% } %>
	<%if(cat.getName().contains("Computer Peripherals")) {%>
					<li style="width:940px; height: 255px;" id="uiv2-slide-one"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/fresh.png') center left no-repeat;">
								<div class="button2"><span>Tender, healthier and tastier<br/> meat straight from the farms!</span><a href='<%="SelectCat.action?catId="+cat.getId()%>'><input type="button" class="button3" value="Explore" /></a> </div>
								</div></li>
		<%}else if(cat.getName().contains("Games")){ %>						
					<li style="width: 940px; height: 255px;" id="uiv2-slide-two"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/frozen.png') center left no-repeat;">
								<div class="button2"><span>Why wait for a season<br/>frozen gives the  same freshness <br/> throughout the year!!</span><a href='<%="SelectCat.action?catId="+cat.getId()%>'><input type="button" class="button3" value="Explore" /></a> </div></div></li>
						<%}else if(cat.getName().contains("DVD Movies")){ %>				
								
					<li style="width: 940px; height: 255px;" id="uiv2-slide-three"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/processed.png') center left no-repeat;">
								<div class="button2"><span>From starters to mains,<br/> prepare anything within no time.</span><a href='<%="SelectCat.action?catId="+cat.getId()%>'><input type="button" class="button3" value="Explore" /></a> </div>
								</div></li>
							<%}else{ %>	
								<% } %>
									<% } %>
					<li style="width: 940px; height: 255px;" id="uiv2-slide-four"><div
								class="uiv2-slider-block-one"
								style="background: url('<%=kkEng.getImageBase()%>/banners/home-page/DeliveryArea.png') center left no-repeat;">
								<div id="pincode"></div>
								<div class="button2" id="go"><input type="text" class="deliveryarea" placeholder="Add your Pincode to help us scale" /><input type="button" class="gobutton" value="Go" /> </div>
								<div class="button2" id="done" style="display:none;"><input type="text" class="deliveryarea" placeholder="Please help us with your email" /><input type="button" class="gobutton" value="Done" /> </div>
								</div></li>
								
				</ul>
			</div>
		
		<%-- <% List<com.konakart.appif.CategoryIf> cats = com.konakart.app.GetCategoryTree.getAllInvisibleCategories();
		%> --%>
 <ul class="uiv2-slides-nav">
<%--  <% for (com.konakart.appif.CategoryIf cat : cats) { 
	 String value= null;
  	//System.out.println("categories=="+cat+" cat.getId=="+cat.getId());
  	if(cat.getId()==1){
  		value="one";
  	}else if(cat.getId()==2){
  		value="two";
  	}else if(cat.getId()==3){
  		value="three";
  	}   	
  	%>
			
				<li class=""><span class="arrow  caption-four"></span><a
					href="#uiv2-slide-<%=value%>"></a><span><%=cat.getName()%></span></li>
				
           
	 <%}%> --%>
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


<%-- <div id="main-menuBanner">
	<%for (int i = 0; i <=3; i++) {%>
		<%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i]; %>
		<%String menuClass; %>
		<%if (i == catMgr.getCats().length-1){ %>
			<% menuClass = "menu-item rounded-corners last-child"; %>
		<% } else { %>
			<% menuClass = "menu-item rounded-corners"; %>
		<% } %>
		<a href='<%="SelectCat.action?catId="+cat.getId()%>' class="<%=menuClass%>" style="width: auto;"><%=cat.getName()%></a>
	<% } %>					
</div> --%>

<%-- <%if (!hideRow2) { %>
	<div id="banners">
		<a href="ShowSpecials.action"><div id="banner-1" class="banner-small rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/electronics-sale.jpg');"></div></a>
		<a href="SelectCat.action?catId=24"><div id="banner-2" class="banner-small rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/gifts-for-the-home.jpg');"></div></a>

		<%if ((int) (Math.random() * 100) > 50) { %>
		<a href="SelectProd.action?prodId=32"><div id="banner-2" class="banner-small rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/windows-8.jpg');"></div></a>
		<% } else { %>
		<a href="SelectCat.action?catId=23"><div id="banner-2" class="banner-small rounded-corners" style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/electronics-sale-2.jpg');"></div></a>
		<% } %> 

		<a href="SelectProd.action?prodId=35"><div id="banner-2" class="banner-small rounded-corners last-child" style="background-image: url('<%=kkEng.getImageBase()%>/banners/home-page/iphone-5.jpg');"></div></a>
	</div>

	
<% } %>  --%>



