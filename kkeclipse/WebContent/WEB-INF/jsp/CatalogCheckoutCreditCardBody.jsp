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

$.validator.addMethod("notExpired", function () {
	var month = document.getElementById('expiryMonth').value;
	var year = '20'+document.getElementById('expiryYear').value;
	var expiry = new Date(year, month-1, 1, 0, 0, 0, 0);
	var now = new Date();
	now.setDate(1);
	now.setHours(0, 0, 0, 0);
	var diff = now - expiry;
	return (diff<=0);
}, '<%=kkEng.getMsg("jquery.validator.card.expired")%>');	 

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
			    		<form action="CheckoutServerPaymentSubmit.action" id="form1" autocomplete="off" method="post"> 
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<p><s:property value="warningMsg" escape="false"/>.</p>
								<div>
									<h3><kk:msg  key="checkout.cc.ccdetails"/><span class="required-text"><img src="<%=kkEng.getImageBase()%>/icons/required-blue.png">&nbsp;<kk:msg  key="common.required.fields"/></span></h3>
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<%if (pd.isShowType()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.type"/></label>
											<select name="type">
												<option value="visa">Visa</option>
												<option value="mastercard">Mastercard</option>
												<option value="amex">American Express</option>
												<option value="diners">Diners</option>
											</select>
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>
									<%}%>
									<%if (pd.isShowOwner()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.ccowner"/></label>
											<input type="text" value="<%=order.getCustomerName()%>" name="owner"/>
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>
									<%}else{%>
										<input type="hidden" name="owner" value="<%=order.getCustomerName()%>">
									<%}%>
									<div class="form-input">
										<label><kk:msg  key="checkout.cc.number"/></label>
										<input type="text"  name="number"/>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>	
									<div class="form-input">
										<label><kk:msg  key="checkout.cc.expiry"/></label>
										<select name="expiryMonth" onChange="javascript:formValidate('form1','','true');" id="expiryMonth">
											<option value="01"><kk:msg  key="month.jan"/></option>
											<option value="02"><kk:msg  key="month.feb"/></option>
											<option value="03"><kk:msg  key="month.mar"/></option>
											<option value="04"><kk:msg  key="month.apr"/></option>
											<option value="05"><kk:msg  key="month.may"/></option>
											<option value="06"><kk:msg  key="month.jun"/></option>
											<option value="07"><kk:msg  key="month.jul"/></option>
											<option value="08"><kk:msg  key="month.aug"/></option>
											<option value="09"><kk:msg  key="month.sep"/></option>
											<option value="10"><kk:msg  key="month.oct"/></option>
											<option value="11"><kk:msg  key="month.nov"/></option>
											<option value="12"><kk:msg  key="month.dec"/></option>
										</select>&nbsp;
										<select name="expiryYear" onChange="javascript:formValidate('form1','','true');" id="expiryYear" >
											<option value="15">2015</option>
											<option value="16">2016</option>
											<option value="17">2017</option>
											<option value="18">2018</option>
											<option value="19">2019</option>
											<option value="20">2020</option>
											<option value="21">2021</option>
											<option value="22">2022</option>
										</select>
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>	
									<%if (pd.isShowCVV()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.cvv"/></label>
											<input type="text" name="cvv"/>
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}%>
									<%if (pd.isShowPostcode()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.postcode"/></label>
											<input type="text" value="<%=order.getBillingPostcode()%>" name="postcode"/>
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}else{%>
										<input type="hidden" name="postcode" value="<%=order.getBillingPostcode()%>">
									<%}%>
									<%if (pd.isShowAddr()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.streetAddress"/></label>
											<input type="text" value="<%=order.getBillingStreetAddress()%>" name="streetAddress"/>
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}else{%>
										<input type="hidden" name="streetAddress" value="<%=order.getBillingStreetAddress()%>">
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
