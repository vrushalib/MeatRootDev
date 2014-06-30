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

 				<h1 id="page-title"><kk:msg  key="header.delivery.details"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="delivery-details">
		    		All the orders till 6pm gets delivered the next day morning and evening. User while placing an
order can choose delivery slot to ensure freshest possible meat.
Slots,
Morning 7am9.30am
Evening 47.30pm
We deliver all seven days.
<br/>
		    			
							<a href="Welcome.action" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.close"/></span></a>
						
			    	</div>
	    		</div>


