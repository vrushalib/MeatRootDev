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

<script type="text/javascript">
$(function() {
	$("#addr-val-dialog").dialog({
		autoOpen: false,
		width: "90%",
		modal: "true",
		hide: "blind",
		open: function( event, ui ) {
			var width = $( "#addr-val-dialog" ).width();
			if (width > 500) {
				$( "#addr-val-dialog" ).dialog( "option", "width", 500 );
			}
		}
	});
});
</script>

   	<div id="addr-val-dialog" title="<kk:msg  key="address.validate.title"/>" class="content-area rounded-corners">
  	        <input type="hidden" id="buttonLabel_conf"/>
  	        <input type="hidden" id="streetAddress_conf"/>
  	        <input type="hidden" id="postcode_conf"/>
  	        <input type="hidden" id="city_conf"/>
		<div class="form-section" id="valid-address">
		</div>
		<div id="addr-val-error">
			<a onclick='javascript:closeAddrValPopup();'  class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
		</div>
		<div id="addr-val-ok">
			<a onclick="javascript:confirmAddr();"  class="button small-rounded-corners continue"><span><kk:msg  key="common.confirm"/></span></a>
			<a onclick='javascript:closeAddrValPopup();' class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
		</div>
    </div>

