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

				<s:set scope="request" var="termsOfUseContent" value="termsOfUseContent"/> 
				<%String termsOfUseContent = (String)(request.getAttribute("termsOfUseContent")); %>
				<h1 id="page-title"><kk:msg  key="header.terms.of.use"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="about-us">
		    			<%=termsOfUseContent%>
						<div class="form-buttons-wide">
							<a href="Welcome.action" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.close"/></span></a>
						</div>
			    	</div>
	    		</div>


