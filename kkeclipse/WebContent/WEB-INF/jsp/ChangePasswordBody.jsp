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
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf cust = customerMgr.getCurrentCustomer();%>

 				<h1 id="page-title"><kk:msg  key="change.password.body.changepassword"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
			    		<form action="ChangePasswordSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<h3><kk:msg  key="change.password.body.mypassword"/></h3>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="form-input">
										<label><kk:msg  key="change.password.body.currentpassword"/></label>
										<input type="password" id="currentPassword" name="currentPassword" autocomplete="off" value=""/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="change.password.body.newpassword"/></label>
										<input type="password" name="password" id="password" autocomplete="off" value=""/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="change.password.body.passwordconfirmation"/></label>
										<input type="password" id="passwordConfirmation" name="passwordConfirmation" autocomplete="off" value=""/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
								</div>
							</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


