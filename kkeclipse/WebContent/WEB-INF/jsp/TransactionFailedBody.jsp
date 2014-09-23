
<%@include file="Taglibs.jsp" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<% com.konakart.al.OrderMgr orderMgr = kkEng.getOrderMgr();%>
<% com.konakart.appif.OrderIf order = orderMgr.getCheckoutOrder();%>

 				<h1 id="page-title"><kk:msg  key="checkout.transaction.failed"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div id="transaction-failed">
			    		<form action="TransactionFailed.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="notification-header">
								    <kk:msg  key="checkout.transaction.failed"/>. &nbsp;Your Order number is <%=order.getId() %>.<br>
									<kk:msg  key="checkout.transaction.failed.long"/>.
								</div>
							</div>
							<div class="form-buttons-wide">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="back.to.orders"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>