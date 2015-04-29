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
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
	
<script type="text/javascript">
	$(function() {
		$.datepicker.setDefaults($.datepicker.regional['<%=kkEng.getLocale().substring(0,2)%>']);
		$( "#datepickerfrom" ).datepicker({changeMonth: true, changeYear: true, dateFormat: '<%=kkEng.getMsg("datepicker.date.format")%>', yearRange: "-10:+1", minDate: '-10y', maxDate: '+1y'});
		$( "#datepickerto" ).datepicker({changeMonth: true, changeYear: true, dateFormat: '<%=kkEng.getMsg("datepicker.date.format")%>', yearRange: "-10:+1", minDate: '-10y', maxDate: '+1y'});

		$("#adv-search-text").keydown(function (e){
		    if(e.keyCode == 13){
		    	formValidate('form1');
		    }
		})	
	
	});
</script>
				<h1 id="page-title"><kk:msg  key="advanced.search.body.advanced.search"/></h1>
	    		<div class="content-area rounded-corners">
		    		<div id="advanced-search">
			    		<form action="ShowSearchResults.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div>
									<h3><kk:msg  key="advanced.search.body.search.text"/></h3>							
								</div>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									
									<div class="form-input">
										<input type="text" id="adv-search-text" name="searchText"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.search.in.descriptions"/></label>
										<s:checkbox name="searchInDescription" theme="simple"/>
									</div>
								</div>
							</div>
							<div class="form-section">
								<h3><kk:msg  key="advanced.search.body.search.filters"/></h3>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.categories"/></label>
										<select name="categoryId" >
											<option  value="<%=kkEng.getSEARCH_ALL()%>"><kk:msg  key="advanced.search.body.all.categories"/></option>
											<% for (int i = 0; i < catMgr.getAllCatsDropList().length; i++){ %>
												<% com.konakart.al.DropListElement dre = catMgr.getAllCatsDropList()[i];%>
												<option  value="<%=dre.getId()%>"><%=dre.getDesc()%></option>
											<% } %>
										</select>
									</div>
									
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.manufacturers"/></label>
										<select name="manufacturerId" >
											<option  value="<%=kkEng.getSEARCH_ALL()%>"><kk:msg  key="advanced.search.body.all.manufacturers"/></option>
											<% for (int i = 0; i < prodMgr.getAllManuDropList().length; i++){ %>
												<% com.konakart.al.DropListElement dre = prodMgr.getAllManuDropList()[i];%>
												<option  value="<%=dre.getId()%>"><%=dre.getDesc()%></option>
											<% } %>
										</select>
									</div>
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.price.from"/></label>
										<input type="text" id="priceFromStr" name="priceFromStr" />
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.price.to"/></label>
										<input type="text" id="priceToStr" name="priceToStr" />
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.date.from"/></label>
										<input type="text" id="datepickerfrom" name="dateAddedFrom"  />
									</div>
									<div class="form-input">
										<label><kk:msg  key="advanced.search.body.date.to"/></label>
										<input type="text" id="datepickerto" name="dateAddedTo" />
									</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="advanced.search.body.search"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


