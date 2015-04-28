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
<% com.konakart.al.RewardPointMgr rewardPointMgr = kkEng.getRewardPointMgr();%>
<% com.konakart.appif.RewardPointIf[] rewardPointArray = rewardPointMgr.getCurrentRewardPoints();%>
<% int points = rewardPointMgr.pointsAvailable();%>

 				<h1 id="page-title"><kk:msg  key="show.rewardpoints.body.rewardpoints" arg0="<%=Integer.toString(points)%>"/></h1>			
	    		<div class="content-area rounded-corners">
	    			<% if (rewardPointArray == null || rewardPointArray.length == 0){ %>
						<kk:msg  key="show.rewardpoints.body.no.rewardpoints"/>.	
					<%} else { %>				
	    		
	    				<div class="reward-points-navigation">
	    					<div class="reward-points-navigation-left">
		    					<span class="number-of-items navigation-element"><%=rewardPointMgr.getCurrentOffset() + 1%>-<%=rewardPointMgr.getNumberOfRewardPoints() + rewardPointMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=rewardPointMgr.getTotalNumberOfRewardPoints()%></span>				    					
			    				<span class="separator"></span>
							</div>
							<div class="reward-points-navigation-right">
		    					<span class="show-per-page navigation-element navigation-dropdown"></span>
			    				<span class="separator"></span>
	    						<kk:paging pageList="<%=rewardPointMgr.getPageList()%>" currentPage="<%=rewardPointMgr.getCurrentPage()%>" showBack="<%=rewardPointMgr.getShowBack()%>" showNext="<%=rewardPointMgr.getShowNext()%>" action="NavigateRewardPoints" timestamp="0"></kk:paging>
							</div>
						</div>
						<div class="reward-points-data">
							
							<div class="reward-points-date-header"><kk:msg  key="show.rewardpoints.body.date"/></div>
							<div class="reward-points-desc-header"><kk:msg  key="show.rewardpoints.body.description"/></div>
							<div class="reward-points-points-header"><kk:msg  key="show.rewardpoints.body.points"/></div>
						
							<% for (int i = 0; i < rewardPointArray.length; i++){ %>
								<% com.konakart.appif.RewardPointIf rp = rewardPointArray[i];%>
								<div class="reward-points-date <%=(i%2==0)?"even":"odd"%>"><%=kkEng.getDateAsString(rp.getDateAdded())%></div>
								<div class="reward-points-desc <%=(i%2==0)?"even":"odd"%>"><%=rp.getDescription()%></div>
								<%if (rp.getTransactionType()==0) { %>
									<div class="reward-points-points <%=(i%2==0)?"even":"odd"%>"><%=rp.getInitialPoints()%></div>
								<%} else { %>
									<div class="reward-points-points <%=(i%2==0)?"even":"odd"%>">(<%=rp.getInitialPoints()%>)</div>
								<%}%>								
							<%}%>	
						</div>
					<%}%>
					<div class="form-buttons-wide">
						<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
					</div>
		    	</div>

