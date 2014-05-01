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

<script type="text/javascript">
	$(function() {
		$.datepicker.setDefaults($.datepicker.regional['<%=kkEng.getLocale().substring(0,2)%>']);
		$('#datepicker').datepicker({changeMonth: true, changeYear: true, dateFormat: '<%=kkEng.getMsg("datepicker.date.format")%>', yearRange: "-120:-10", minDate: '-120y', maxDate: '-10y'});
	});
</script>	

 				<h1 id="page-title"><kk:msg  key="edit.customer.myaccount"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
			    		<form action="EditCustomerSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div>
									<h3><kk:msg  key="register.customer.body.personal.details"/><span class="required-text"><img src="<%=kkEng.getImageBase()%>/icons/required-blue.png">&nbsp;<kk:msg  key="common.required.fields"/></span></h3>							
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<%if (kkEng.getConfigAsBoolean("ACCOUNT_GENDER",false)) { %>
										<div class="form-input radio-buttons">
											<label><kk:msg  key="register.customer.body.gender"/></label>
											<s:set scope="request" var="gender" value="gender"/> 						
											<% String g = (String)request.getAttribute("gender");%> 
											<span class="radio-button"><input type="radio" name="gender" value="m" <%=(g!=null&&g.equals("m"))?"checked":"" %>> <kk:msg  key="register.customer.body.male"/></span> 
											<span class="radio-button"><input type="radio" name="gender" value="f" <%=(g!=null&&g.equals("f"))?"checked":"" %>> <kk:msg  key="register.customer.body.female"/></span>	
											<span class="required-icon required-blue"></span>	
											<span class="validation-msg"></span>							
										</div>
									<% } else { %>
										<input type="hidden" name="gender" value="-"/>
									<% } %>
									
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.first.name"/></label>
										<input type="text" value="<s:property value="firstName" />" name="firstName"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.last.name"/></label>
										<input type="text" value="<s:property value="lastName" />" name="lastName"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<%if (kkEng.getConfigAsBoolean("ACCOUNT_DOB",false)) { %>
										<div class="form-input">
											<label><kk:msg  key="register.customer.body.dob"/></label>
											<input id="datepicker" type="text" value="<s:property value="birthDateString" />" name="birthDateString"/>
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>
									<% } else { %>
										<input type="hidden" name="birthDateString" value="<%=kkEng.getNowAsString()%>"/>
									<% } %>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.email"/></label>
										<input type="text" value="<s:property value="emailAddr" />"  name="emailAddr"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
								</div>
							</div>
							<div class="form-section">
								<h3><kk:msg  key="register.customer.body.contact.info"/></h3>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.tel.number"/></label>
										<input type="text" value="<s:property value="telephoneNumber" />" name="telephoneNumber" />
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.tel.number1"/></label>
										<input type="text" value="<s:property value="telephoneNumber1" />" name="telephoneNumber1" />
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.fax.number"/></label>
										<input type="text" value="<s:property value="faxNumber" />" name="faxNumber" />
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


