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
<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>

 <div id="breadcrumbs">
	<%if (kkEng.getNav().getNavigation().length > 0){%>
    	<span class="breadcrumb-item"><a href="Welcome.action"><kk:msg  key="header.top"/></a></span>
	<%}%>												
	<%for (int i = 0; i < kkEng.getNav().getNavigation().length; i++) {%>
    	<%String n = kkEng.getNav().getNavigation()[i]; %> 
    	<%if (n!=null){%>
            <%String[] tokens = n.split(";");%>
			<%if (tokens.length>=2){%>
				<span class="breadcrumb-separator"></span>				
				<% String link = tokens[1]; %>
				<%if (tokens.length>2){%>
  					<%for (int j = 2; j < tokens.length; j++){%>
      					<%link = link + tokens[j];%>
					<%}%>
				<%}%>
				<span class="breadcrumb-item"><a href="<%=link%>"><%=tokens[0]%></a></span>
			<%}else{%>
				<span class="breadcrumb-separator"></span>
				<span class="breadcrumb-item"><%=n%></span>
			<%}%>		    												
		<%}%>		    												
	<%}%> 	
 </div>