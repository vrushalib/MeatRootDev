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
<% com.konakart.appif.ZoneIf[] zoneArray = customerMgr.getSelectedZones();%>
<% com.konakart.appif.AddressIf addr = customerMgr.getSelectedAddr();%>

<%@include file="AddrValPopup.jsp" %>

				<h1 id="page-title"><kk:msg  key="edit.addr.body.title"/></h1>
	    		<div class="content-area rounded-corners">
		    		<div>
			    		<form action="EditAddrSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<input type="hidden" name="opcbilling" value="<s:property value="opcbilling" />"/>
							<input type="hidden" name="opcdelivery" value="<s:property value="opcdelivery" />"/>
							<s:set scope="request" var="opcbilling" value="opcbilling"/> 						
							<s:set scope="request" var="opcdelivery" value="opcdelivery"/> 						
							<% String opcbilling = (String)request.getAttribute("opcbilling");%> 
							<% String opcdelivery = (String)request.getAttribute("opcdelivery");%> 	
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
								</div>
							</div>
							<%if (kkEng.getConfigAsBoolean("ACCOUNT_COMPANY",false)) { %>
								<div class="form-section">
									<h3><kk:msg  key="register.customer.body.company.details"/></h3>
									<div class="form-section-fields">
										<div class="form-section-divider"></div>
										<div class="form-input">
											<label><kk:msg  key="register.customer.body.company.name"/></label>
											<input type="text" value="<s:property value="company" />" name="company"/>
											<span class="validation-msg"></span>
										</div>
									</div>
								</div>
							<% } else { %>
								<input type="hidden" name="company" value=""/>
							<% } %>
							<div class="form-section">
								<h3><kk:msg  key="register.customer.body.addr"/></h3>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.street.addr"/></label>
										<input type="text" value="<s:property value="streetAddress" />" name="streetAddress"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<%if (kkEng.getConfigAsBoolean("ACCOUNT_STREET_ADDRESS_1",false)) { %>
										<div class="form-input">
											<label><kk:msg  key="register.customer.body.street.addr1"/></label>
											<input type="text" value="<s:property value="streetAddress1" />" name="streetAddress1" />
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>
									<% } else { %>
										<input type="hidden" name="streetAddress1"  value=""/>
									<% } %>
									<%if (kkEng.getConfigAsBoolean("ACCOUNT_SUBURB",false)) { %>
										<div class="form-input">
											<label><kk:msg  key="register.customer.body.suburb"/></label>
											<input  type="text" value="<s:property value="suburb" />" name="suburb"/>
											<span class="validation-msg"></span>
										</div>
									<% } else { %>
										<input type="hidden" name="suburb" value=""/>
									<% } %>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.postcode"/></label>
										<input type="text" value="<s:property value="postcode" />" name="postcode" />
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.city"/></label>
										<input type="text" value="<s:property value="city" />" name="city" />
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<%if (kkEng.getConfigAsBoolean("ACCOUNT_STATE",false)) { %>
										<%if (zoneArray != null && zoneArray.length > 0){ %>	
											<div class="form-input">
												<label><kk:msg  key="register.customer.body.state"/></label>
												<select name="state" class="state">
													<option value="-1"><kk:msg  key="register.customer.body.select"/></option>
													<s:set scope="request" var="state"  value="state"/> 						
													<% String state = ((String)request.getAttribute("state"));%> 
													<% for (int i = 0; i < zoneArray.length; i++){ %>
														<% com.konakart.appif.ZoneIf zone = zoneArray[i];%>
														<%if (state != null && state.equals(zone.getZoneName())){ %>
															<option  value="<%=zone.getZoneName()%>" selected="selected"><%=zone.getZoneName()%></option>
														<% } else { %>
															<option  value="<%=zone.getZoneName()%>"><%=zone.getZoneName()%></option>
														<% } %>
													<% } %>
												</select>
												<span class="required-icon required-blue"></span>
												<span class="validation-msg"></span>
											</div>
										<% } else {%>
											<div class="form-input">
												<label><kk:msg  key="register.customer.body.state"/></label>
												<input type="text" name="state" value="<s:property value="state" />" id="state"/>
												<span class="required-icon required-blue"></span>
												<span class="validation-msg"></span>
											</div>
										<% } %>
									<% } else { %>
										<input type="hidden" name="state" value="-----"/>
									<% } %>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.country"/></label>
											<select name="countryId" class="country" onchange="javascript:changeCountry();">
												<option value="-1"><kk:msg  key="register.customer.body.select"/></option>
													<%com.konakart.appif.CountryIf selectedCountry = kkEng.getCustomerMgr().getSelectedCountry(); %>
													<% com.konakart.appif.CountryIf[] countries = kkEng.getAllCountries();%>
													<%if (countries != null){%>
													    <%for ( int i = 0; i < countries.length; i++)	{%>
													        <%com.konakart.appif.CountryIf country = countries[i];%>
															<%if (selectedCountry != null && country.getId() == selectedCountry.getId()){ %>
																<option selected="selected" value="<%=Integer.toString(country.getId())%>"><%=country.getName()%></option>
															<% } else { %>
																<option value="<%=Integer.toString(country.getId())%>"><%=country.getName()%></option>
															<% } %>														                 
														<% } %>           
													<% } %>																
											</select>
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
										<input type="text" value="<s:property value="telephoneNumber" />" id="telephoneNumber" name="telephoneNumber" />
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.tel.number1"/></label>
										<input type="text" value="<s:property value="telephoneNumber1" />" name="telephoneNumber1" />
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="register.customer.body.email"/></label>
										<input type="text" value="<s:property value="emailAddrOptional" />"  name="emailAddrOptional"/>
										<span class="validation-msg"></span>
									</div>
								</div>
							</div>
							<%if (!addr.getIsPrimary()){ %>
								<div class="form-section">
									<h3><kk:msg  key="new.addr.body.primary"/></h3>
									<div class="form-section-fields">
										<div class="form-section-divider"></div>
										<div class="form-input">
											<label><kk:msg  key="edit.addr.body.setasprimary"/></label>
											<s:checkbox name="setAsPrimaryBool" theme="simple"/>
										</div>
									</div>
								</div>
							<% } %>
							<div class="form-buttons">
								<a onclick="javascript:addrValidate('form1');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.update"/></span></a>
								<%if ((opcbilling == null || opcbilling.length() == 0) && (opcdelivery == null || opcdelivery.length() == 0)){ %>
									<a href="AddressBook.action" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
								<% } else { %>
									<a href="CheckoutNoReset.action" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
								<% } %>				
							</div>
							<input type="hidden" id="countryChange" name="countryChange" value="0"/>
						</form>
			    	</div>
	    		</div>


