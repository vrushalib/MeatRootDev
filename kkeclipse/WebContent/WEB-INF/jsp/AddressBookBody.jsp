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

 				<h1 id="page-title"><kk:msg  key="address.book.body.mypersonaladdressbook"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
						<s:if test="hasActionMessages()">
						   <div class="messageStackSuccess">  
						        <s:iterator value="actionMessages">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    
			    		<form action="EditCustomerSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="form-section-title">
									<h3><kk:msg  key="address.book.body.primaryaddress"/></h3>							
								</div>
								<div class="addr-book-header">
									<div class="addr-book-explanation">
										<kk:msg  key="address.book.body.addrexplanation"/>
									</div>
									<div class="addr-book-addr">
										<%if (cust.getAddresses() != null && cust.getAddresses().length > 0){ %>
											<%=kkEng.removeCData(cust.getAddresses()[0].getFormattedAddress())%>
										<% } %>
									</div>
								</div>
							</div>
							<div class="form-section">
								<div class="form-section-title no-margin">
									<h3><kk:msg  key="address.book.body.addressbookentries"/><a href="NewAddr.action"  class="button-medium new-addr-button small-rounded-corners"><span><kk:msg  key="address.book.body.newaddress"/></span></a></h3>									
								</div>
								<%if (cust.getAddresses() != null && cust.getAddresses().length > 0){ %>
									<% for (int i = 0; i < cust.getAddresses().length; i++){ %>
										<% com.konakart.appif.AddressIf addr = cust.getAddresses()[i];%>
										<div class="select-addr-section <%=(i%2==0)?"even":"odd"%>">
											<div class="select-addr">
												<%if (i == 0){ %>
													<span class="primary-addr-label">(<kk:msg  key="address.book.body.primaryaddress"/>)</span><br>
												<% } %>											
												<%=kkEng.removeCData(addr.getFormattedAddress())%>
											</div>
											<div class="select-addr-buttons">
												<a href='<%="EditAddr.action?addrId="+addr.getId()%>' class="button-medium small-rounded-corners">
													<span><kk:msg  key="common.edit"/></span>
												</a>&nbsp;
												<%if (i != 0){ %>
													<a href='<%="DeleteAddr.action?addrId="+addr.getId()%>' class="button-medium small-rounded-corners">
														<span><kk:msg  key="common.delete"/></span>
													</a>											
												<% } %>											
											</div>
										</div>
									<% } %>
								<% } %>
							</div>
							<div class="form-buttons-wide">
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


