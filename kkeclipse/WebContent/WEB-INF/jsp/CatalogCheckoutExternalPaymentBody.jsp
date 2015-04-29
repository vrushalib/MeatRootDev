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
<% com.konakart.al.OrderMgr orderMgr = kkEng.getOrderMgr();%>
<% com.konakart.appif.OrderIf order = orderMgr.getCheckoutOrder();%>
<% com.konakart.appif.PaymentDetailsIf pDetails = order.getPaymentDetails();%>


<html>
	<head>
		<% if (pDetails.getPostOrGet().equals("redirect")){ %>
			<meta http-equiv="REFRESH" content="2;url=<%=pDetails.getRequestUrl()%>">
		<% } %>
	</head>
	<% if (pDetails.getPostOrGet().equals("redirect")){ %>		
		<body>
			<h4><kk:msg  key="external.payments.body.message"/></h4>
		</body>
	<% } else { %>
		<body onload="document.forms[0].submit();">
			<h4><kk:msg  key="external.payments.body.message"/></h4>
		<%if (pDetails.getParameters() != null && pDetails.getParameters().length > 0){ %>
			<form  action="<%=pDetails.getRequestUrl()%>" method="<%=pDetails.getPostOrGet()%>" method="post">  
				<% for (int i = 0; i < pDetails.getParameters().length; i++){ %>
					<% com.konakart.appif.NameValueIf parm = pDetails.getParameters()[i];%>
					<input type="hidden" name="<%=parm.getName()%>" value="<%=parm.getValue()%>">
				<% } %>
				<noscript>
					<br/>
					<br/>
					<div style="text-align: center">
						<h1><kk:msg  key="external.payments.body.message.heading"/></h1>
						<p><kk:msg  key="external.payments.body.message.clickmsg"/></p>
						<input type="submit" class="button" value="<kk:msg  key="common.continue"/>"/>
					</div>
				</noscript>
			</form>
		<% } %>
		</body>
	<% } %>
</html>














