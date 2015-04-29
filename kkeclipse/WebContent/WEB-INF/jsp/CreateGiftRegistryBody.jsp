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

<script type="text/javascript">
	$(function() {
		$.datepicker.setDefaults($.datepicker.regional['<%=kkEng.getLocale().substring(0,2)%>']);
		$('#datepicker').datepicker({changeMonth: true, changeYear: true, dateFormat: '<%=kkEng.getMsg("datepicker.date.format")%>', yearRange: "-1:+10", minDate: '-1y', maxDate: '+10y'});
		});	
</script>	

 				<h1 id="page-title"><kk:msg  key="create.gift.registry.body.create.wedding.list"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
			    		<form action="CreateGiftRegistrySubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div>
									<h3><kk:msg  key="create.gift.registry.body.details"/><span class="required-text"><img src="<%=kkEng.getImageBase()%>/icons/required-blue.png">&nbsp;<kk:msg  key="common.required.fields"/></span></h3>							
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>								
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.event.name"/></label>
										<input type="text" value="<s:property value="registryName" />" id="registryName" name="registryName"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.event.link.url"/></label>
										<input type="text" value="<s:property value="linkURL" />" id="linkURL" name="linkURL"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.groom.name"/></label>
										<input type="text" value="<s:property value="firstName" />" id="firstName" name="firstName"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.groom.last.name"/></label>
										<input type="text" value="<s:property value="lastName" />" id="lastName" name="lastName"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.bride.name"/></label>
										<input type="text" value="<s:property value="firstName1" />" id="firstName1" name="firstName1"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.bride.last.name"/></label>
										<input type="text" value="<s:property value="lastName1" />" id="lastName1" name="lastName1"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.event.date"/></label>
										<input id="datepicker" type="text" value="<s:property value="eventDateString" />" name="eventDateString"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input radio-buttons">
										<label><kk:msg  key="create.gift.registry.body.is.public"/></label>
										<s:set scope="request" var="publicWishList" value="publicWishList"/> 						
										<% String p = (String)request.getAttribute("publicWishList");%> 
										<span class="radio-button"><input type="radio" name="publicWishList" value="true" checked> <kk:msg  key="create.gift.registry.body.public"/></span> 
										<span class="radio-button"><input type="radio" name="publicWishList" value="false"> <kk:msg  key="create.gift.registry.body.private"/></span>	
										<span class="validation-msg"></span>							
									</div>
								</div>
							</div>
							<div class="form-section">
								<h3><kk:msg  key="create.gift.registry.body.shippingaddress"/></h3>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="gift-reg-shipping-desc">
										<kk:msg  key="create.gift.registry.body.addrexplanation"/>
									</div>
									<div class="gift-reg-shipping-addr">
										<%if (cust.getAddresses() != null && cust.getAddresses().length > 0){ %>
											<%=kkEng.removeCData(cust.getAddresses()[0].getFormattedAddress())%>
											<input type="hidden" name = "addressId" value="<%=new Integer(cust.getAddresses()[0].getId()).toString()%>"/>
										<%}%>
									</div>
								</div>
							</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
							<input type="hidden" name="listType" value="<%=new Integer(com.konakart.al.WishListMgr.WEDDING_LIST_TYPE).toString()%>"/>
						</form>
			    	</div>
	    		</div>


