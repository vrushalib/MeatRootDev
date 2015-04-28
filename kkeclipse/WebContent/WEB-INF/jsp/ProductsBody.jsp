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

<%@page import="com.konakart.al.ProductMgr"%>
<%@page import="com.konakart.app.DataDescConstants"%>

<script type="text/javascript">
 
$(function() {
	
	$( window ).resize(function() {
		layoutItems();
	});

	layoutItems();

});


function layoutItems() {
	var width = $(".items").width();
	var tile = $(".item:first");
	if (tile != null) {
				
		var tileTotalWidth = 180;
		if (width < 440) {
			tileTotalWidth = 150;
		} 
		
		var numItems = Math.floor(width / tileTotalWidth);
		var extra = width - (numItems*tileTotalWidth);
		var extraPerItem  = Math.floor(extra / numItems);
		
		var extraLeft = Math.ceil(extraPerItem/2);
		if (extraLeft > 0) {
			extraLeft-= 1;
		}
		var extraRight = Math.floor(extraPerItem/2);
		
		$(".items li").each(function(index){
			var item = $(this);
			item.css("margin-left",extraLeft+'px');
			item.css("margin-right",extraRight+'px');
			item.width(tileTotalWidth);
		});						
	}
}

</script>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<% com.konakart.al.ProductMgr prodMgr = kkEng.getProductMgr();%>
<% com.konakart.appif.ProductIf[] prodArray = prodMgr.getCurrentProducts();%>
<% com.konakart.appif.SuggestedSpellingItemIf[] suggestedSpellings = prodMgr.getSuggestedSpellings();%>
<% com.konakart.appif.CategoryIf currentCat = prodMgr.getSelectedCategory();%>
<% com.konakart.appif.ManufacturerIf currentManu = prodMgr.getSelectedManufacturer();%>
<% String sortBy = prodMgr.getDataDesc().getOrderBy();%>


 <%if (prodArray == null || prodArray.length == 0){ %>
 	<%if (suggestedSpellings == null || suggestedSpellings.length == 0){ %>
   		<div id="item-overview" class="item-area <%=kkEng.getContentClass()%> rounded-corners">
			<div class="item-area-header"></div>
			<div class="items-message">	
				<%if (prodMgr.isExpiredResultSet()){ %>
					<kk:msg  key="products.body.products.have.expired"/>.
				<% } else { %>
					<kk:msg  key="products.body.there.are.no.products.to.list.in.this.category"/>.	    					
				<% } %>
			</div>
			<div class="item-area-footer"></div>
		</div>
	<% } else { %>
		<div id="item-overview" class="item-area <%=kkEng.getContentClass()%> rounded-corners">
			<div class="item-area-header"></div>
			<div class="items-message">	
				<kk:msg  key="products.body.suggested.spelling" arg0="<%=org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(prodMgr.getSuggestedSpellingSearch())%>"/>
				<div class="spelling-suggestions">
					<ul>
					<% for (int i = 0; i < suggestedSpellings.length; i++){ %>
						<% com.konakart.appif.SuggestedSpellingItemIf ssi = suggestedSpellings[i];%>
						<li>
							<a href="QuickSearch.action?searchText=<%=ssi.getSuggestion()%>&searchInDesc=true" class="text-link"><span><%=ssi.getSuggestion()%></span></a>
						</li>
					<%}%>
					</ul>
				</div>
			</div>
			<div class="item-area-footer"></div>
		</div>		
	<% } %>
<% } else { %>
 	    		<div id="item-overview" class="item-area <%=kkEng.getContentClass()%> rounded-corners">
					<div class="item-area-header">
	    				<div class="item-overview-area-navigation">
	    					<div class="item-overview-area-navigation-left">
		    					<span class="number-of-items navigation-element">
		    					<%=prodMgr.getCurrentOffset() + 1%>-<%=prodMgr.getNumberOfProducts() + prodMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=prodMgr.getTotalNumberOfProducts()%> <kk:msg  key="products.body.products"/>
		    					</span>
			    				<span class="separator"></span>
								<span class="sort-by navigation-element navigation-dropdown">
									<form action="SortProd.action" method="post"><kk:msg  key="common.sort.by"/>:
										<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
										<select name="orderBy" onchange="submit()">					
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.viewed"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.sold"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.asc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.desc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.rating"/></option>
										</select>
										<input type="hidden" name="t" value="<%=prodMgr.getProdTimestamp()%>"/>
									</form>	
								</span>
							</div>
							<div class="item-overview-area-navigation-right">
								<kk:pageSize action="ProdPageSize.action" name="numProds" sizes="8,12,16,20,24,28" maxNum="<%=prodMgr.getMaxDisplaySearchResults()%>" timestamp="<%=prodMgr.getProdTimestamp()%>"/>
			    				<span class="separator"></span>
			    				<kk:paging pageList="<%=prodMgr.getPageList()%>" currentPage="<%=prodMgr.getCurrentPage()%>" showBack="<%=prodMgr.getShowBack()%>" showNext="<%=prodMgr.getShowNext()%>" action="NavigateProd"  timestamp="<%=prodMgr.getProdTimestamp()%>"></kk:paging>
							</div>
	    				</div>
	    				<div class="item-overview-area-navigation-mobile">
	    					<div class="item-overview-area-navigation-top">
		    					<span class="number-of-items navigation-element">
		    					<%=prodMgr.getCurrentOffset() + 1%>-<%=prodMgr.getNumberOfProducts() + prodMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=prodMgr.getTotalNumberOfProducts()%> <kk:msg  key="products.body.products"/>
		    					</span>
			    			
								
								<kk:paging pageList="<%=prodMgr.getPageList()%>" currentPage="<%=prodMgr.getCurrentPage()%>" showBack="<%=prodMgr.getShowBack()%>" showNext="<%=prodMgr.getShowNext()%>" action="NavigateProd"  timestamp="<%=prodMgr.getProdTimestamp()%>"></kk:paging>
							</div>
							<div class="item-overview-area-navigation-bottom">
								<kk:pageSize action="ProdPageSize.action" name="numProds" sizes="8,12,16,20,24,28" maxNum="<%=prodMgr.getMaxDisplaySearchResults()%>" timestamp="<%=prodMgr.getProdTimestamp()%>" type="small"/>
			    				
			    				<span class="sort-by navigation-element navigation-dropdown">
									<form action="SortProd.action" method="post"><kk:msg  key="common.sort"/>:
										<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
										<select name="orderBy" onchange="submit()">					
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.viewed"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.sold"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.asc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.desc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.rating"/></option>
										</select>
										<input type="hidden" name="t" value="<%=prodMgr.getProdTimestamp()%>"/>
									</form>	

								</span>
			    				
							</div>
	    				</div>
	    			</div>
	    			<div class="items">	    			
	    				<%int numProds =  (prodArray.length > prodMgr.getMaxDisplaySearchResults()) ? prodMgr.getMaxDisplaySearchResults() : prodArray.length;%>
	 					<ul>
	 					<% for (int i = 0; i < numProds; i++){ %>
							<% com.konakart.appif.ProductIf prod = prodArray[i];%>
							<li>
							<kk:prodTile prod="<%=prod%>"/>
							</li>
						<%}%>
						</ul>
	    			</div>

	       			<div class="item-area-footer">
	    				<div class="item-overview-area-navigation">
	    					<div class="item-overview-area-navigation-left">
		    					<span class="number-of-items navigation-element">
		    					<%=prodMgr.getCurrentOffset() + 1%>-<%=prodMgr.getNumberOfProducts() + prodMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=prodMgr.getTotalNumberOfProducts()%> <kk:msg  key="products.body.products"/>
		    					</span>
			    				<span class="separator"></span>
								<span class="sort-by navigation-element navigation-dropdown">
									<form action="SortProd.action" method="post"><kk:msg  key="common.sort.by"/>:
										<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
										<select name="orderBy" onchange="submit()">					
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.viewed"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.sold"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.asc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.desc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.rating"/></option>
										</select>
										<input type="hidden" name="t" value="<%=prodMgr.getProdTimestamp()%>"/>
									</form>	
								</span>
							</div>
							<div class="item-overview-area-navigation-right">
								<kk:pageSize action="ProdPageSize.action" name="numProds" sizes="8,12,16,20,24,28" maxNum="<%=prodMgr.getMaxDisplaySearchResults()%>" timestamp="<%=prodMgr.getProdTimestamp()%>"/>
			    				<span class="separator"></span>
			    				<kk:paging pageList="<%=prodMgr.getPageList()%>" currentPage="<%=prodMgr.getCurrentPage()%>" showBack="<%=prodMgr.getShowBack()%>" showNext="<%=prodMgr.getShowNext()%>" action="NavigateProd"  timestamp="<%=prodMgr.getProdTimestamp()%>"></kk:paging>
							</div>
	    				</div>
	    				<div class="item-overview-area-navigation-mobile">
	    					<div class="item-overview-area-navigation-top">
		    					<span class="number-of-items navigation-element">
		    					<%=prodMgr.getCurrentOffset() + 1%>-<%=prodMgr.getNumberOfProducts() + prodMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=prodMgr.getTotalNumberOfProducts()%> <kk:msg  key="products.body.products"/>
		    					</span>
			    			
								
								<kk:paging pageList="<%=prodMgr.getPageList()%>" currentPage="<%=prodMgr.getCurrentPage()%>" showBack="<%=prodMgr.getShowBack()%>" showNext="<%=prodMgr.getShowNext()%>" action="NavigateProd"  timestamp="<%=prodMgr.getProdTimestamp()%>"></kk:paging>
							</div>
							<div class="item-overview-area-navigation-bottom">
								<kk:pageSize action="ProdPageSize.action" name="numProds" sizes="8,12,16,20,24,28" maxNum="<%=prodMgr.getMaxDisplaySearchResults()%>" timestamp="<%=prodMgr.getProdTimestamp()%>" type="small"/>
			    				
			    				<span class="sort-by navigation-element navigation-dropdown">
									<form action="SortProd.action" method="post"><kk:msg  key="common.sort"/>:
										<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
										<select name="orderBy" onchange="submit()">					
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.viewed"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.most.sold"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_ASCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_ASCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.asc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_PRICE_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_PRICE_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.price.desc"/></option>
											<option  value="<%=DataDescConstants.ORDER_BY_RATING_DESCENDING%>" <%=(sortBy.equals(DataDescConstants.ORDER_BY_RATING_DESCENDING)?"selected=\"selected\"":"") %>><kk:msg  key="products.body.sort.products.by.rating"/></option>
										</select>
										<input type="hidden" name="t" value="<%=prodMgr.getProdTimestamp()%>"/>
									</form>	

								</span>
			    				
							</div>
	    				</div>
	    			</div>
	    		</div>

<% } %>