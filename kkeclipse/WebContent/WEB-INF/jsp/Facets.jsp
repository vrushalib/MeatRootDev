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
<%@page import="java.math.BigDecimal"%>
<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<% com.konakart.al.ProductMgr prodMgr = kkEng.getProductMgr();%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
<% boolean useSolr = kkEng.isUseSolr();%>

 
	<%if (prodMgr.getMinPrice() != null && prodMgr.getMaxPrice() != null && prodMgr.getMinPrice().compareTo(prodMgr.getMaxPrice()) != 0){%>
		<%BigDecimal minPrice = prodMgr.getMinPrice();%>
		<%BigDecimal maxPrice = prodMgr.getMaxPrice();%>
		<%BigDecimal taxMult = prodMgr.getTaxMultiplier();%>
		<%BigDecimal minFilterPrice = (prodMgr.getProdSearch().getPriceFrom()==null)?minPrice:prodMgr.getProdSearch().getPriceFrom();%>
		<%BigDecimal maxFilterPrice = (prodMgr.getProdSearch().getPriceTo()==null)?maxPrice:prodMgr.getProdSearch().getPriceTo();%>
		
		<%if (kkEng.displayPriceWithTax() && taxMult != null && !taxMult.equals(new BigDecimal(1))){%>	
			<%minPrice = minPrice.multiply(taxMult);%>				
			<%maxPrice = maxPrice.multiply(taxMult);%>				
			<%minFilterPrice = minFilterPrice.multiply(taxMult);%>				
			<%maxFilterPrice = maxFilterPrice.multiply(taxMult);%>				
		<%}%>

		<% minPrice = ((kkEng.convertPrice(minPrice)).setScale(2,BigDecimal.ROUND_HALF_UP)).setScale(0,BigDecimal.ROUND_DOWN);%>
		<% maxPrice = ((kkEng.convertPrice(maxPrice)).setScale(2,BigDecimal.ROUND_HALF_UP)).setScale(0,BigDecimal.ROUND_UP);%>
		<% minFilterPrice = ((kkEng.convertPrice(minFilterPrice)).setScale(2,BigDecimal.ROUND_HALF_UP)).setScale(0,BigDecimal.ROUND_DOWN);%>
		<% maxFilterPrice = ((kkEng.convertPrice(maxFilterPrice)).setScale(2,BigDecimal.ROUND_HALF_UP)).setScale(0,BigDecimal.ROUND_UP);%>
		
		<%String symbol = kkEng.getUserCurrency().getSymbolLeft();%>
		<script>
		    $(function() {
		        $( "#price-range-slider" ).slider({
		            range: true,
		            min: <%=minPrice%>,
		            max: <%=maxPrice%>,
		            values: [ <%=minFilterPrice%>, <%=maxFilterPrice%> ],
		            slide: function( event, ui ) {
		                $( "#amount" ).html( "<%=symbol%>"+ui.values[ 0 ] + " - " + "<%=symbol%>"+ui.values[ 1 ] );
		            },
			        stop: function( event, ui ) {
						document.getElementById('priceFromStr').value = ui.values[ 0 ];
						document.getElementById('priceToStr').value = ui.values[ 1 ];
						document.getElementById('priceFilterForm').submit();
			        }
		        });
		        
		        $( "#amount" ).html("<%=symbol%>"+$( "#price-range-slider" ).slider( "values", 0 ) +
		            " - " +"<%=symbol%>"+ $( "#price-range-slider" ).slider( "values", 1 ) );
		        
		        $( "#price-range-slider-mobile" ).slider({
		            range: true,
		            min: <%=minPrice%>,
		            max: <%=maxPrice%>,
		            values: [ <%=minFilterPrice%>, <%=maxFilterPrice%> ],
		            slide: function( event, ui ) {
		                $( "#amount-mobile" ).html( "<%=symbol%>"+ui.values[ 0 ] + " - " + "<%=symbol%>"+ui.values[ 1 ] );
		            },
			        stop: function( event, ui ) {
						document.getElementById('priceFromStr').value = ui.values[ 0 ];
						document.getElementById('priceToStr').value = ui.values[ 1 ];
						document.getElementById('priceFilterForm').submit();
			        }
		        });
		        $( "#amount-mobile" ).html("<%=symbol%>"+$( "#price-range-slider-mobile" ).slider( "values", 0 ) +
		            " - " +"<%=symbol%>"+ $( "#price-range-slider-mobile" ).slider( "values", 1 ) );
		    });
		</script> 
	<% } %>

	<script>

		if ($(window).width() < 750) {
			  $(function() {
			    $( "#side-menu-mobile" ).accordion({
			    	collapsible: true,
			    	active: false,
			    	heightStyle: "content"
			    });
			  });
		}else{
			window.onresize = function() {
				if ($(window).width() < 750) {
				  $(function() {
				    $( "#side-menu-mobile" ).accordion({
				    	collapsible: true,
				    	active: false,
				    	heightStyle: "content"
				    });
				  });
				}
			};
		}

	  </script>

	<div id="side-menu">	
  			<% if (prodMgr.getCurrentCategoriesLength() > 0){%>				
				<div class="side-menu-section">
					<h1><kk:msg  key="facet.tile.categories"/></h1>
					<ul>				
					<% for (int i = 0; i < prodMgr.getCurrentCategories().length; i++){ %>
						<% com.konakart.appif.CategoryIf cat = prodMgr.getCurrentCategories()[i];%>
						<%String name = (cat.getNumberOfProducts() < 0)? cat.getName(): cat.getName()+" ("+cat.getNumberOfProducts()+")"; %>
						<li>
						<%for (int j = 0; j < cat.getLevel(); j++){%>
							<%="<span class='fa fa-angle-right'></span>"%>
						<% }%>
						<%String action;%>
						<%if (prodMgr.getNumSelectedFilters() > 0 || prodMgr.isPriceFilter()){%>
							<%action= "FilterSearchByCategory.action?catId=";%>
						<% } else { %>
							<%action= "SelectCat.action?catId=";%>
						<% }%>
						
						<%if ( cat.isSelected()) { %>
							<a href='<%=action+cat.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="current-cat"><%=name%></span></a>
						<% } else { %>
							<a href='<%=action+cat.getId()+"&t="+prodMgr.getProdTimestamp()%>'><%=name%></a>
						<% } %>
						</li>
					<% } %>
					</ul>
				</div>
				<%
				boolean haveManus = prodMgr.getCurrentManufacturersLength() > 0;
				boolean havePriceFacets = prodMgr.getPriceFacets() != null && prodMgr.getPriceFacets().length > 0;
				boolean havePriceSlider = prodMgr.getMinPrice() != null && prodMgr.getMaxPrice() != null && prodMgr.getMinPrice().compareTo(prodMgr.getMaxPrice()) != 0;
				boolean haveFacets = prodMgr.getCurrentTagGroups() != null && prodMgr.getCurrentTagGroups().length > 0;				
				%>
				<%if (haveManus || havePriceFacets || havePriceSlider || haveFacets){%>
					<h1>
						<kk:msg  key="facet.tile.refine.search"/>	
					</h1>
				<%}%>
				<%if (prodMgr.getNumSelectedFilters() > 0 || prodMgr.isPriceFilter()){%>					
					<div id="remove-all"><img  src="<%=kkEng.getImageBase()%>/x-button.png"><a href='<%="RemoveTags.action?t="+prodMgr.getProdTimestamp()%>'><kk:msg  key="products.body.clear.filters"/></a></div>				
				<%}%>
				    						
				<%if (haveManus){ %>
					<div class="side-menu-section">
						<h2><kk:msg  key="facet.tile.manufacturers"/></h2>	
						<ul>				
							<% for (int i = 0; i < prodMgr.getCurrentManufacturers().length; i++){ %>
								<% com.konakart.appif.ManufacturerIf manu = prodMgr.getCurrentManufacturers()[i];%>
								<%String name = manu.getName()+" ("+manu.getNumberOfProducts()+")"; %>
								<%if ( manu.isSelected()) { %>
									<li><a href='<%="FilterSearchByManufacturer.action?manuId="+manu.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-selected"></span><%=name%></a></li>	
								<% } else { %>
									<li><a href='<%="FilterSearchByManufacturer.action?manuId="+manu.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-not-selected"></span><%=name%></a></li>	
								<% } %>
							<% } %>
						</ul>
					</div>
				<% } %>				
				<%if (havePriceFacets){%>
					<div class="side-menu-section">
						<h2><kk:msg  key="facet.tile.price.range"/></h2>	
						<ul>	
							<%BigDecimal taxMult = prodMgr.getTaxMultiplier();%>			
							<% for (int i = 0; i < prodMgr.getPriceFacets().length; i++){ %>
								<% com.konakart.appif.KKPriceFacetIf pf = prodMgr.getPriceFacets()[i];%>
								<%BigDecimal lowerLimit =pf.getLowerLimit();%>
								<%BigDecimal upperLimit =pf.getUpperLimit();%>
								<%if (kkEng.displayPriceWithTax() && taxMult != null && !taxMult.equals(new BigDecimal(1))){%>	
									<%lowerLimit = lowerLimit.multiply(taxMult);%>				
									<%upperLimit = upperLimit.multiply(taxMult);%>				
								<%}%>
								
								<%String name = kkEng.convertPrice(lowerLimit).setScale(0,BigDecimal.ROUND_HALF_DOWN)+" - "+kkEng.convertPrice(upperLimit).setScale(0,BigDecimal.ROUND_HALF_UP) + " ("+pf.getNumProds()+")"; %>
								<%if (pf.isSelected()) { %>
									<li><a href='<%="FilterSearchByPrice.action?from="+pf.getLowerLimit()+"&to="+pf.getUpperLimit()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-selected"></span><%=name%></a></li>
								<% } else { %>
									<li><a href='<%="FilterSearchByPrice.action?from="+pf.getLowerLimit()+"&to="+pf.getUpperLimit()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-not-selected"></span><%=name%></a></li>
								<% } %>
							<% } %>
						</ul>
					</div>
				<% } else if (havePriceSlider){%>
					<div id="price" class="range-slider">
		    			<h2><kk:msg  key="common.price"/></h2>
						<div id="price-range-slider"></div>
						<div id="amount"></div>
	    			</div>
					<form action="FilterSearchByPrice.action" id='priceFilterForm' method="post">
						<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
						<input id="priceFromStr" name="priceFromStr" type="hidden"/>
						<input id="priceToStr" name="priceToStr" type="hidden"/>
						<input id="timestamp" name="timestamp" type="hidden" value="<%=prodMgr.getProdTimestamp()%>"/>
						<input id="taxMultiplier" name="taxMultiplier" type="hidden" value="<%=prodMgr.getTaxMultiplier()%>"/>
					</form>
				<% } %>
				<%if (haveFacets){ %>
					<div class="side-menu-section">
					<% String previousName=""; %>
					<% for (int i = 0; i < prodMgr.getCurrentTagGroups().length; i++){ %>
						<% com.konakart.appif.TagGroupIf tagGroup = prodMgr.getCurrentTagGroups()[i];%>
						<%if (tagGroup.getName() != null && previousName != null && !tagGroup.getName().equals(previousName)){ %>
							<h2><%=tagGroup.getName()%></h2>
						<% } %>
						<% previousName = tagGroup.getName(); %>
						<%if (tagGroup.getTags() != null && tagGroup.getTags().length > 0){ %>
							<ul>
							<% for (int j = 0; j < tagGroup.getTags().length; j++){ %>
								<% com.konakart.appif.TagIf tag = tagGroup.getTags()[j];%>
								<%String name = (useSolr)? kkEng.getMsg(tag.getName()): tag.getName(); %>
								<%if ( tag.isSelected()) { %>
									<li><a href='<%="FilterSearchByTags.action?tagId="+tag.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-selected"></span><%=name+" ("+tag.getNumProducts()+")"%></a></li>
								<% } else { %>
									<li><a href='<%="FilterSearchByTags.action?tagId="+tag.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-not-selected"></span><%=name+" ("+tag.getNumProducts()+")"%></a></li>
								<% } %>
							<% } %>
							</ul>
						<% } %>
					<% } %>	
					</div>
				<% } %>
			<%}%>		    																											
	</div>
	<div id="side-menu-mobile">	
  			<% if (prodMgr.getCurrentCategoriesLength() > 0){%>				
				
				<h1><kk:msg  key="facet.tile.categories"/></h1>
				<div class="side-menu-section">	
					<ul>				
					<% for (int i = 0; i < prodMgr.getCurrentCategories().length; i++){ %>
						<% com.konakart.appif.CategoryIf cat = prodMgr.getCurrentCategories()[i];%>
						<%String name = (cat.getNumberOfProducts() < 0)? cat.getName(): cat.getName()+" ("+cat.getNumberOfProducts()+")"; %>
						<li>
						<%for (int j = 0; j < cat.getLevel(); j++){%>
							<%="<span class='fa fa-angle-right'></span>"%>
						<% }%>
						<%String action;%>
						<%if (prodMgr.getNumSelectedFilters() > 0 || prodMgr.isPriceFilter()){%>
							<%action= "FilterSearchByCategory.action?catId=";%>
						<% } else { %>
							<%action= "SelectCat.action?catId=";%>
						<% }%>
						
						<%if ( cat.isSelected()) { %>
							<a href='<%=action+cat.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="current-cat"><%=name%></span></a>
						<% } else { %>
							<a href='<%=action+cat.getId()+"&t="+prodMgr.getProdTimestamp()%>'><%=name%></a>
						<% } %>
						</li>
					<% } %>
					</ul>
				</div>
				<%
				boolean haveManus = prodMgr.getCurrentManufacturersLength() > 0;
				boolean havePriceFacets = prodMgr.getPriceFacets() != null && prodMgr.getPriceFacets().length > 0;
				boolean havePriceSlider = prodMgr.getMinPrice() != null && prodMgr.getMaxPrice() != null && prodMgr.getMinPrice().compareTo(prodMgr.getMaxPrice()) != 0;
				boolean haveFacets = prodMgr.getCurrentTagGroups() != null && prodMgr.getCurrentTagGroups().length > 0;				
				%>
				<%if (haveManus || havePriceFacets || havePriceSlider || haveFacets){%>
					<h1>
						<kk:msg  key="facet.tile.refine.search"/>	
					</h1>
					<div id="refine-search-content">
						<%if (prodMgr.getNumSelectedFilters() > 0 || prodMgr.isPriceFilter()){%>					
							<div id="remove-all"><img  src="<%=kkEng.getImageBase()%>/x-button.png"><a href='<%="RemoveTags.action?t="+prodMgr.getProdTimestamp()%>'><kk:msg  key="products.body.clear.filters"/></a></div>				
						<%}%>
						    						
						<%if (haveManus){ %>
						
							<h2><kk:msg  key="facet.tile.manufacturers"/></h2>
							<div class="side-menu-section">
								<ul>				
									<% for (int i = 0; i < prodMgr.getCurrentManufacturers().length; i++){ %>
										<% com.konakart.appif.ManufacturerIf manu = prodMgr.getCurrentManufacturers()[i];%>
										<%String name = manu.getName()+" ("+manu.getNumberOfProducts()+")"; %>
										<%if ( manu.isSelected()) { %>
											<li><a href='<%="FilterSearchByManufacturer.action?manuId="+manu.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-selected"></span><%=name%></a></li>	
										<% } else { %>
											<li><a href='<%="FilterSearchByManufacturer.action?manuId="+manu.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-not-selected"></span><%=name%></a></li>	
										<% } %>
									<% } %>
								</ul>
							</div>
						<% } %>				
						<%if (havePriceFacets){%>
							
							<h2><kk:msg  key="facet.tile.price.range"/></h2>	
								<div class="side-menu-section">
								<ul>	
									<%BigDecimal taxMult = prodMgr.getTaxMultiplier();%>			
									<% for (int i = 0; i < prodMgr.getPriceFacets().length; i++){ %>
										<% com.konakart.appif.KKPriceFacetIf pf = prodMgr.getPriceFacets()[i];%>
										<%BigDecimal lowerLimit =pf.getLowerLimit();%>
										<%BigDecimal upperLimit =pf.getUpperLimit();%>
										<%if (kkEng.displayPriceWithTax() && taxMult != null && !taxMult.equals(new BigDecimal(1))){%>	
											<%lowerLimit = lowerLimit.multiply(taxMult);%>				
											<%upperLimit = upperLimit.multiply(taxMult);%>				
										<%}%>
										
										<%String name = kkEng.convertPrice(lowerLimit).setScale(0,BigDecimal.ROUND_HALF_DOWN)+" - "+kkEng.convertPrice(upperLimit).setScale(0,BigDecimal.ROUND_HALF_UP) + " ("+pf.getNumProds()+")"; %>
										<%if (pf.isSelected()) { %>
											<li><a href='<%="FilterSearchByPrice.action?from="+pf.getLowerLimit()+"&to="+pf.getUpperLimit()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-selected"></span><%=name%></a></li>
										<% } else { %>
											<li><a href='<%="FilterSearchByPrice.action?from="+pf.getLowerLimit()+"&to="+pf.getUpperLimit()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-not-selected"></span><%=name%></a></li>
										<% } %>
									<% } %>
								</ul>
							</div>
						<% } else if (havePriceSlider){%>
				    		<div class="side-menu-section">
						    	<div id="price" class="range-slider">
					    			<h2><kk:msg  key="common.price"/></h2>
									<div id="price-range-slider-mobile"></div>
									<div id="amount-mobile"></div>
				    			</div>			    			
								<form action="FilterSearchByPrice.action" id='priceFilterForm' method="post">
									<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
									<input id="priceFromStr" name="priceFromStr" type="hidden"/>
									<input id="priceToStr" name="priceToStr" type="hidden"/>
									<input id="timestamp" name="timestamp" type="hidden" value="<%=prodMgr.getProdTimestamp()%>"/>
									<input id="taxMultiplier" name="taxMultiplier" type="hidden" value="<%=prodMgr.getTaxMultiplier()%>"/>
								</form>
							</div>
						<% } %>
						<%if (haveFacets){ %>
							
							<% String previousName=""; %>
							<% for (int i = 0; i < prodMgr.getCurrentTagGroups().length; i++){ %>
								<% com.konakart.appif.TagGroupIf tagGroup = prodMgr.getCurrentTagGroups()[i];%>
								<%if (tagGroup.getName() != null && previousName != null && !tagGroup.getName().equals(previousName)){ %>
									<h2><%=tagGroup.getName()%></h2>
								<% } %>
								<div class="side-menu-section">
								<% previousName = tagGroup.getName(); %>
								<%if (tagGroup.getTags() != null && tagGroup.getTags().length > 0){ %>
									<ul>
									<% for (int j = 0; j < tagGroup.getTags().length; j++){ %>
										<% com.konakart.appif.TagIf tag = tagGroup.getTags()[j];%>
										<%String name = (useSolr)? kkEng.getMsg(tag.getName()): tag.getName(); %>
										<%if ( tag.isSelected()) { %>
											<li><a href='<%="FilterSearchByTags.action?tagId="+tag.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-selected"></span><%=name+" ("+tag.getNumProducts()+")"%></a></li>
										<% } else { %>
											<li><a href='<%="FilterSearchByTags.action?tagId="+tag.getId()+"&t="+prodMgr.getProdTimestamp()%>'><span class="kk-not-selected"></span><%=name+" ("+tag.getNumProducts()+")"%></a></li>
										<% } %>
									<% } %>
									</ul>
								<% } %>
							</div>
							<% } %>							
						<% } %>
						</div>
					<%}%>	
				<%}%>	
					    																											
	</div>
 
