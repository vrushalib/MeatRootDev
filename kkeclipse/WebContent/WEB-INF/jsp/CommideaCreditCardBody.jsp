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
<% com.konakart.appif.PaymentDetailsIf pd = order.getPaymentDetails();%>

<script type="text/javascript">

$(function() {
	$('#loading-img').hide();	
});	

jQuery.validator.addMethod("expiry", function(exp_date, element) {
	return this.optional(element) || 
	exp_date.match(/^((0[1-9])|(1[0-2]))(\d{2})$/);	
}, '<%=kkEng.getMsg("jquery.validator.expiryMMYY")%>');

</script>

 				<h1 id="page-title"><kk:msg  key="checkout.cc.payment"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="credit-card">
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
			    		<form id="form1" action="<%=pd.getCustom4()%>" method="post" autocomplete="off">
						 	<input type="hidden" name="sessionguid" value="<%=(pd.getCustom1().split(";"))[0]%>">
						 	<input type="hidden" name="sessionpasscode" value="<%=pd.getCustom2()%>">
						 	<input type="hidden" name="processingdb" value="<%=(pd.getCustom1().split(";"))[1]%>">
							<div class="form-section">
								<p><s:property value="warningMsg" escape="false"/>.</p>
								<div>
									<h3><kk:msg  key="checkout.cc.ccdetails"/><span class="required-text"><img src="<%=kkEng.getImageBase()%>/icons/required-blue.png">&nbsp;<kk:msg  key="common.required.fields"/></span></h3>							
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="form-input">
										<label><kk:msg  key="checkout.cc.number"/></label>
										<input type="text" name="pan" id="pan" class="required creditcard" >
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>	
									<div class="form-input">
										<label><kk:msg  key="checkout.cc.expiry"/> (MMYY)</label>
										<input type="text" name="expirydate" id="expirydate" class="required expiry" >
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>	
									<%if (pd.isShowCVV()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.cvv"/></label>
											<input type="text" name="csc" id="csc" class="required digits" minlength="3" maxlength="4" >
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}%>
									<%if (pd.isShowOwner()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.ccowner"/></label>
											<input type="text" name="cardholdername" id="cardholdername" class="required" value="<%=order.getCustomerName()%>">
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}else{%>
										<input type="hidden" name="cardholdername" value="<%=order.getCustomerName()%>">
									<%}%>
									<%if (pd.isShowPostcode()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.postcode"/></label>
											<input type="text" name="postcode" id="postcode" class="required" value="<%=order.getBillingPostcode()%>">
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}else{%>
										<input type="hidden" name="postcode" value="<%=order.getBillingPostcode()%>">
									<%}%>
									<%if (pd.isShowAddr()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.streetAddress"/></label>
											<input type="text" name="address1" id="address1" class="required" value="<%=order.getBillingStreetAddress()%>">
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}else{%>
										<input type="hidden" name="address1" value="<%=order.getBillingStreetAddress()%>">
									<%}%>
								</div>
							</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
								<a href="CheckoutNoReset.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


