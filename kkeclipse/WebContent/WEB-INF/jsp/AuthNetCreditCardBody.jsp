<%--
//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

$.validator.addMethod("notExpired", function () {
	var expStr = document.getElementById('x_exp_date').value;
	var month = expStr.substring(0,2);
	var year = '20'+expStr.substring(2,4);
	var expiry = new Date(year, month-1, 1, 0, 0, 0, 0);
	var now = new Date();
	now.setDate(1);
	now.setHours(0, 0, 0, 0);
	var diff = now - expiry;
	return (diff<=0);
}, '<%=kkEng.getMsg("jquery.validator.card.expired")%>');

</script>
 				<h1 id="page-title"><kk:msg  key="checkout.cc.payment"/></h1>				    			    		
	    		<%if (pd.getCreditCards() != null && pd.getCreditCards().length > 0) {%>
	    			<div class="content-area rounded-corners">
	    				<p><kk:msg  key="checkout.cc.stored.cards.explanation"/></p>
		   				<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		  					
   					</div>
	    			<h1></h1>
		    		<div class="content-area rounded-corners">
   					<div>
						<h3><kk:msg  key="checkout.cc.stored.cards"/></h3>							
					</div>
					<div class="form-section-fields">
						<div class="form-section-divider"></div>		    		
				    	<% for (int i = 0; i < pd.getCreditCards().length; i++){ %>
							<% com.konakart.appif.CreditCardIf cc = pd.getCreditCards()[i];%>
							<div class="select-cc-section <%=(i%2==0)?"even":"odd"%>">
								<div class="select-cc">
									<span class="cc-data"><kk:msg  key="checkout.cc.number"/>:</span>&nbsp;<span class="kk-bold"><%=cc.getCcNumber()%></span></br>
									<span class="cc-data"><kk:msg  key="checkout.cc.ccowner"/>:</span>&nbsp;<span class="kk-bold"><%=cc.getCcOwner()%></span>
								</div>
								<div class="pay-now-buttons">
									<a href='<%="AuthorizeNetCIMPay.action?id="+cc.getCcIdentifier()%>' class="button-medium small-rounded-corners">
										<span><kk:msg  key="common.pay.now"/></span>
									</a>
								</div>
							</div>
						<%}%>
		    		</div>
		    	</div>
		    	<h1></h1>
				<%}%>

	    		<div class="content-area rounded-corners">
		    		<%if (pd.getCreditCards() == null || pd.getCreditCards().length == 0) {%>
		   				<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		
					<%}%>				    		    		  					
		    		<div id="credit-card">
			    		<form id="form1" action="<%=pd.getRequestUrl()%>" method="post" autocomplete="off">
							<div class="form-section">
								<p><s:property value="warningMsg" escape="false"/>.</p>
								<div>
									<h3><kk:msg  key="checkout.cc.ccdetails"/><span class="required-text"><img src="<%=kkEng.getImageBase()%>/icons/required-blue.png">&nbsp;<kk:msg  key="common.required.fields"/></span></h3>							
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="form-input">
										<label><kk:msg  key="checkout.cc.number"/></label>
										<input type="text" name="x_card_num" id="x_card_num" class="required creditcard" >
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>	
									<div class="form-input">
										<label><kk:msg  key="checkout.cc.expiry"/> (MMYY)</label>
										<input type="text" name="x_exp_date" id="x_exp_date" class="required expiry notExpired" >
										<span class="required-icon required-blue"></span>
										<span class="validation-msg"></span>
									</div>	
									<%if (pd.isShowCVV()) {%>
										<div class="form-input">
											<label><kk:msg  key="checkout.cc.cvv"/></label>
											<input type="text" name="x_card_code" id="x_card_code" class="required digits" minlength="3" maxlength="4" >
											<span class="required-icon required-blue"></span>
											<span class="validation-msg"></span>
										</div>	
									<%}%>
								</div>
							</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
								<a href="CheckoutNoReset.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
							<% for (int i = 0; i < pd.getParameters().length; i++){ %>
								<% com.konakart.appif.NameValueIf nv = pd.getParameters()[i];%>
								<input type="hidden" name="<%=nv.getName()%>" value="<%=nv.getValue()%>">
							<%}%>
						</form>
			    	</div>
	    		</div>


