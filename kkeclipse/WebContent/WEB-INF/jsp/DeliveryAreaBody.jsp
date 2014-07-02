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


	<script type="text/javascript">	
		$(function() {
			$("#password").keydown(function (e){
			    if(e.keyCode == 13){
			    	formValidate('form1', 'continue-button');
			    }
			});
		});				
	</script>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf currentCustomer = customerMgr.getCurrentCustomer();%>

 
    		<h1 id="page-title"><kk:msg  key="header.delivery.area"/></h1>
 	    		<div id="new-account-area" class="content-area rounded-corners">
				    <s:if test="hasActionErrors()">
					   <div class="messageStackError">  
					        <s:iterator value="actionErrors">  
					            <s:property escape="false"/>
					        </s:iterator>  
		    			</div>  
					</s:if>		    
					<s:if test="hasActionMessages()">
					   <div class="messageStackSuccess">  
					        <s:iterator value="actionMessages">  
					            <s:property escape="false"/>
					        </s:iterator>  
		    			</div>  
					</s:if>		    
		    		
			    		<form action="DeliveryArea.action" id="form1" method="post"> 
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
			    			<div class="form-section">
		    			
		    				<div class="form-input">
									<label><kk:msg  key="login.body.name"/></label>
									<input type="text" id="name" name="Name">
									<span class="validation-msg"></span>	
								</div>
								<div class="form-input">
									<label><kk:msg  key="login.body.email"/></label>
									<input type="text" id="emailAddr" name="emailAddr">
									<span class="validation-msg"></span>	
								</div>
								<div class="form-input">
									<label><kk:msg  key="login.body.Pincode"/></label>
									<input type="text" id="pincode" name="PinCode">
									<span class="validation-msg"></span>	
								</div>
								
								
							</div>
						</form>
			    	
				
	    		</div>






	




