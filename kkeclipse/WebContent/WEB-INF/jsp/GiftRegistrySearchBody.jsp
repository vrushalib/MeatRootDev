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
		$('#datepicker').datepicker({changeMonth: true, changeYear: true, dateFormat: '<%=kkEng.getMsg("datepicker.date.format")%>', yearRange: "-1:+10", minDate: '-1y', maxDate: '+10y'});
		
		$("#form1").validate(validationRules);
		$('#customerFirstName').rules("add", { maxlength: 32 });
		$('#customerLastName').rules("add", { maxlength: 32 });
		$('#customer1FirstName').rules("add", { maxlength: 32 });
		$('#customer1LastName').rules("add", { maxlength: 32 });
	
	});	
</script>	

 				<h1 id="page-title"><kk:msg  key="giftregistry.search.body.search.weddinglists"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
			    		<form action="ShowGiftRegistrySearchResults.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div>
									<h3><kk:msg  key="giftregistry.search.body.search.criteria"/></h3>							
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>								
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.groom.name"/></label>
										<input type="text" id="customerFirstName" value="<s:property value="customerFirstName" />" name="customerFirstName"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.groom.last.name"/></label>
										<input type="text" id="customerLastName" value="<s:property value="customerLastName" />" name="customerLastName"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.bride.name"/></label>
										<input type="text" id="customer1FirstName" value="<s:property value="customer1FirstName" />" name="customer1FirstName"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.bride.last.name"/></label>
										<input type="text" id="customer1LastName" value="<s:property value="customer1LastName" />" name="customer1LastName"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="create.gift.registry.body.event.date"/></label>
										<input id="datepicker" type="text" value="<s:property value="eventDateStringSearch" />" name="eventDateStringSearch"/>
										<span class="validation-msg"></span>
									</div>
								</div>
							</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="advanced.search.body.search"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


