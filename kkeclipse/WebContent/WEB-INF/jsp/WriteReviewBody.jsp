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
<% com.konakart.al.ProductMgr prodMgr = kkEng.getProductMgr();%>
<% com.konakart.appif.ProductIf prod = prodMgr.getSelectedProduct();%>
<% com.konakart.al.ReviewMgr revMgr = kkEng.getReviewMgr();%>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.CustomerIf currentCustomer = customerMgr.getCurrentCustomer();%>
	

				<h1 id="page-title"><kk:msg  key="write.review.body.title" arg0="<%=prod.getName()%>"/></h1>
	    		<div class="content-area rounded-corners">
		    		<div>
			    		<form action="WriteReviewSubmit.action" id="form1" method="post">
							<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="review-input">
									<div class="form-input">
										<label><kk:msg  key="write.review.body.your.review"/></label>
										<textarea  name="reviewText"></textarea>
										<span class="validation-msg"></span>
									</div>
								</div>
							</div>
							<div class="form-section">
								<div class="review-input">
									<div class="form-input">
										<span class="review-rating-buttons">
											<label><kk:msg  key="write.review.body.rating"/></label>
											<span class="rating-text"><kk:msg  key="write.review.body.bad"/></span> 
											<input type="radio" name="rating"  value="1"/>
											<input type="radio" name="rating"  value="2"/>
											<input type="radio" name="rating"  value="3"/>
											<input type="radio" name="rating"  value="4"/>
											<input type="radio" name="rating"  value="5"/>
											<span class="rating-text"><kk:msg  key="write.review.body.good"/></span> 
										</span>
										<span class="validation-msg"></span>										
									</div>
								</div>
							</div>
							<div class="form-buttons-write-review">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.submit"/></span></a>
								<a href='<%="SelectProd.action?prodId="+prod.getId()%>' class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>							                        				
							</div>
						</form>
			    	</div>
	    		</div>


