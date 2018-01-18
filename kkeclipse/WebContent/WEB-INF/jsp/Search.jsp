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
<%@page import="com.konakart.app.Content"%>
<%@page import="com.konakart.app.ContentDescription"%>
<%@page import="com.konakart.appif.ContentIf"%>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% boolean useSolr = kkEng.isUseSolr();%>
<% boolean showCookieWarning = !kkEng.isAgreedCookies();%>
<% boolean contentEnabled = kkEng.getContentMgr().isEnabled();%>
<% String contentDir = kkEng.getContentImagesDir();%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>

<%if (useSolr) { %>						
	<script type="text/javascript">	
	/*
	 * Autocomplete widget
	 */
	$(function() {
		$( "#search-input" ).autocomplete({
			source: function(request, response) {
				if (document.getElementById('kk_portlet_id'))  {
					AUI().ready('liferay-portlet-url', function(A) { 
				        var renderURL = Liferay.PortletURL.createResourceURL();
				        renderURL.setParameter("struts.portlet.action", "/SuggestedSearch.action");
				        renderURL.setPortletId(document.getElementById('kk_portlet_id').value);
				        renderURL.setWindowState("exclusive");
						renderURL.setParameter("term", request.term);
						renderURL.setParameter("categoryId", $("#top-cat-list").val());
						
						$.ajax({
						type : 'POST',
						timeout : '20000',
						scriptCharset : "utf-8",
						contentType : "application/json; charset=utf-8",
						url : renderURL.toString(),
						dataType : 'json',
						data : null,
					       success: function(result, textStatus, jqXHR) {					         
					      		response(result);
					      }
					    });
					});	
				} else {
				     $.ajax({
				 		type : 'POST',
						timeout : '20000',
						scriptCharset : "utf-8",
						contentType : "application/json; charset=utf-8",
						url : "SuggestedSearch.action",
						dataType : 'json',
						data : '{"categoryId":"'+$("#top-cat-list").val()+'",'+'"term":"' + request.term + '"}',
				        success: function(result, textStatus, jqXHR) {					         
				       		response(result);
				       }
				     });
				}
			   },
			minLength: 1,
			select: function( event, ui ) {
				var catId = $("#top-cat-list").val();
				self.kkSearch(ui.item.id,ui.item.value,catId);
			}
		}).data( "uiAutocomplete" )._renderItem = function( ul, item ) {
			   ul.addClass('ui-corner-all');
	           return $( "<li class='ui-corner-all'></li>" )
	               .data( "item.autocomplete", item )
	               .append( "<a>"+ item.label + "</a>" )
	               .appendTo( ul );
		};
		
		
		$("#search-button").click(function (){
		    	var key = document.getElementById('kk_key').value;
			    var text = document.getElementById('search-input').value;
			    var catId = $("#top-cat-list").val();
		    	self.kkSearch(key,text,catId);
		});		
	});	
	</script>
	
	<!-- For posting suggested search query -->
	<form action="" id='ssForm' method="post">
		<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
		<input id="searchText" name="searchText" type="hidden"/>
		<input id="manuId" name="manuId" type="hidden"/>
		<input id="catId" name="catId" type="hidden"/>
	</form>
<% } %>	

<%if (showCookieWarning) { %>	
	<div id="cookie-container">
		<div id="cookie-warning">
				<span style="display:table-cell; vertical-align:top;"><kk:msg  key="cookie.warning"/></span>
				<span style="display:table-cell; vertical-align:middle;"><div id="cookie-warn-button" class="button small-rounded-corners"><kk:msg  key="common.continue"/></div></span>
		</div>
	</div>
<% } %>

<div id="header-container">
	<div id="header">
		<div id="logo">
			<% ContentIf logoImg = null;%>
			<%if (contentEnabled) { %>
				<% ContentIf[] logoImgs = kkEng.getContentMgr().getContentForType(1, 13);%>
				<%if (logoImgs.length == 1) { %>
					<% logoImg = logoImgs[0];%>
				<% } %> 
			<% } %> 

			<%if (logoImg == null) { %>
				<% logoImg = new Content();%>
				<% logoImg.setDescription(new ContentDescription());%>
				<% logoImg.getDescription().setName1("logo.png");%>
				<% logoImg.getDescription().setTitle("KonaKart logo");%>
				<% logoImg.setClickUrl("Welcom.action");%>
			<% } %> 
			<a href="<%=logoImg.getClickUrl()%>"><img id="logo-1"  
			    src="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=logoImg.getDescription().getName1()%>"
			    alt="<%=logoImg.getDescription().getTitle()%>"/></a>
		</div>
		<div id="search">
			<%if (useSolr) { %>						
				<select id="top-cat-list" class="rounded-corners-left">
					<option  value="-1"><kk:msg  key="suggested.search.all"/></option>
					<%for (int i = 0; i < catMgr.getCats().length; i++) {%>
						<%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i]; %>
						<%if (kkEng.getSearchParentCategoryId() == cat.getId()){ %>
							<option  value="<%=cat.getId()%>" selected="selected"><%=cat.getName()%></option>
						<% } else { %>
							<option  value="<%=cat.getId()%>"><%=cat.getName()%></option>
						<% } %>						
					<% } %>					
				</select>
				<input type="text" id="search-input"  name="searchText" onkeydown="javascript:kkKeydown();">
				<input id="kk_key" type="hidden"/>
				<a id="search-button" class="rounded-corners-right"><kk:msg  key="suggested.search.search"/></a>
			<% } else { %>	
				<form action="QuickSearch.action" id="quickSearchForm" method="post">
					<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
					<input type="hidden" value="true" name="searchInDesc"/>
					<input type="text" id="search-input" name="searchText" class="rounded-corners-left">
					<a id="search-button" class="rounded-corners-right" onclick="javascript:document.getElementById('quickSearchForm').submit();"><kk:msg  key="suggested.search.search"/></a>
				</form>	
            <% } %>
		</div>
		<a id="adv-search-link" href="AdvancedSearch.action"><kk:msg  key="header.advanced.search"/></a>
 	</div>
</div>