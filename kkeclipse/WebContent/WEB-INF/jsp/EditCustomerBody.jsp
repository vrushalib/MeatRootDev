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
											<% boolean otherGenderEnabled = kkEng.getConfigAsBoolean("ENABLE_OTHER_GENDER",false); %>
											<% String radioClass = "radio-button"; %>
											<%if (otherGenderEnabled) { %>
												<% radioClass = "radio-button-1-and-2-of-3"; %>
											<% } %>
											<s:set scope="request" var="gender" value="gender"/>
											<% String g = (String)request.getAttribute("gender");%> 
											<span class="<%=radioClass%>"><input type="radio" name="gender" value="m" <%=(g!=null&&g.equals("m"))?"checked":"" %>> <kk:msg  key="register.customer.body.male"/></span> 
											<span class="<%=radioClass%>"><input type="radio" name="gender" value="f" <%=(g!=null&&g.equals("f"))?"checked":"" %>> <kk:msg  key="register.customer.body.female"/></span>
											<%if (otherGenderEnabled) { %>
												<span class="radio-button-3-of-3"><input type="radio" name="gender" value="x" <%=(g!=null&&g.equals("x"))?"checked":"" %>> <kk:msg  key="register.customer.body.other"/></span>
											<% } %>
											<span class="required-icon required-blue"></span>	
											<span class="validation-msg"></span>							
										</div>
									<% } else { %>
										<input type="hidden" name="gender" value="-"/>
									<% } %>
									
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.first.name"/></label>
										<input type="text" value="<s:property value="firstName" escape="false"/>" name="firstName"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.last.name"/></label>
										<input type="text" value="<s:property value="lastName" escape="false"/>" name="lastName"/>
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
									<%if (kkEng.getConfigAsBoolean("ACCOUNT_TAX_ID",false)) { %>
										<div class="form-input">
											<label><kk:msg  key="register.customer.body.tax.id"/></label>
											<input type="text" value="<s:property value="taxId" />"  name="taxId"/>
											<span class="validation-msg"></span>
										</div>
									<% } else { %>
										<input type="hidden" name="taxId" value=""/>
									<% } %>										
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


