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
<% com.konakart.al.OrderMgr orderMgr = kkEng.getOrderMgr();%>
<%boolean enableInvoice=kkEng.getConfigAsBoolean("ENABLE_PDF_INVOICE_DOWNLOAD", false); %>

 				<h1 id="page-title"><kk:msg  key="show.all.orders.myorderhistory"/></h1>			
	    		<div id="show-all-orders" class="content-area rounded-corners">
	    			<%if (orderMgr.getCurrentOrders() == null || orderMgr.getCurrentOrders().length == 0){ %>
						<kk:msg  key="show.all.orders.nopurchases"/>.	
					<%} else { %>				
	    		
	    				<div class="order-navigation">
	    					<div class="order-navigation-left">
		    					<span class="number-of-items navigation-element"><%=orderMgr.getCurrentOffset() + 1%>-<%=orderMgr.getNumberOfOrders() + orderMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=orderMgr.getTotalNumberOfOrders()%></span>				    					
			    				<span class="separator"></span>
							</div>
							<div class="order-navigation-right">
		    					<kk:pageSize action="OrderPageSize.action" name="numOrders" sizes="5,10,20,30,50" maxNum="<%=orderMgr.getPageSize()%>"/>
			    				<span class="separator"></span>
	    						<kk:paging pageList="<%=orderMgr.getPageList()%>" currentPage="<%=orderMgr.getCurrentPage()%>" showBack="<%=orderMgr.getShowBack()%>" showNext="<%=orderMgr.getShowNext()%>" action="NavigateAllOrders" timestamp="0"></kk:paging>
							</div>
						</div>
						<div class="order-data">
								<% for (int i = 0; i < orderMgr.getCurrentOrders().length; i++){ %>
									<% com.konakart.appif.OrderIf order = orderMgr.getCurrentOrders()[i];%>
									<% int numItems = (order.getOrderProducts()!=null)?order.getOrderProducts().length:0;%>
									<% String statusClass = (order.getStatusText()!=null&&order.getStatusText().equalsIgnoreCase("delivered"))?"shipped":"pending";%>
					    			<div class="all-orders">
					    				<table>				    				
				    						<tr>
				    							<td class="narrow-col">#<%=order.getId()%></td>
				    							<td class="narrow-col"><%=kkEng.getDateAsString(order.getDatePurchased())%></td>
				    							<td class="narrow-col"><kk:msg  key="common.total"/>: <%=kkEng.formatPrice(order.getTotalIncTax(),order.getCurrencyCode())%></td>
				    							<td class="status-col"><div class="label <%=statusClass%>"><%=order.getStatusText()%></div></td>
				    							<td class="narrow-col"><a class="text-link" href='<%="ShowOrderDetails.action?orderId="+order.getId()%>'><kk:msg  key="common.view"/></a></td>	
				    							<td class="narrow-col"><a class="text-link" href='<%="RepeatOrder.action?orderId="+order.getId()%>'><kk:msg  key="common.repeat"/></a></td>	
				    							<td class="narrow-col"><a class="text-link"><kk:msg  key="common.track"/></a></td>	
												<%if (enableInvoice) {%>	
													<%if (kkEng.isPortlet()){ %>
														<td class="narrow-col"><a class="text-link" href='<%="DownloadInvoicePortlet.action?orderId="+order.getId()%>'><kk:msg  key="common.invoice"/></a></td>
													<%} else {%>
														<td class="narrow-col"><a class="text-link" href='<%="DownloadInvoice.action?orderId="+order.getId()%>'><kk:msg  key="common.invoice"/></a></td>
													<%}%>
												<% } %>
			    							</tr>
				    						<%if (order.getOrderProducts() != null && order.getOrderProducts().length > 0){ %>
					    						<tr>
					    							<td colspan="8">	    					
							    						 <table>
									    					<thead>
									    						<tr>
									    							<td class="wide-col"><kk:msg  key="common.item"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.quantity"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.total"/></td>		    						
									    						</tr>
									    					</thead>
									    					<tbody>				    						
							    							<% for (int j = 0; j < order.getOrderProducts().length; j++){ %>	
							    								<% com.konakart.appif.OrderProductIf orderProd = order.getOrderProducts()[j];%>
									    						<tr>
									    							<td><a class="text-link" href='<%="SelectProd.action?prodId="+orderProd.getProductId()%>'><%=orderProd.getName()%></a></td>
									    							<td class="right"><%=orderProd.getQuantity()%></td>
									    							<%if (kkEng.displayPriceWithTax()) {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceIncTax(),order.getCurrencyCode())%></td>
																	<%} else {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceExTax(),order.getCurrencyCode())%></td>
																	<%}%>	
									    						</tr>
							    							<% } %>
									    					</tbody>	    				
									    				</table>
								    				</td>
					    						</tr>
					    					<% } %>		    							    							    							    				
					    				</table> 
					    			</div>																	
				    			<% } %>
						</div>

						<div class="order-data-mobile">
								<% for (int i = 0; i < orderMgr.getCurrentOrders().length; i++){ %>
									<% com.konakart.appif.OrderIf order = orderMgr.getCurrentOrders()[i];%>
									<% int numItems = (order.getOrderProducts()!=null)?order.getOrderProducts().length:0;%>
									<% String statusClass = (order.getStatusText()!=null&&order.getStatusText().equalsIgnoreCase("delivered"))?"shipped":"pending";%>
					    			<div class="all-orders">
					    				<table>				    				
				    						<tr>
				    							<td class="id-col">#<%=order.getId()%></td>
				    							<td class="date-col"><%=kkEng.getDateAsString(order.getDatePurchased())%></td>
				    							
				    							<td class="status-col"><div class="label <%=statusClass%>"><%=order.getStatusText()%></div></td>
				    							<td class="all-orders-icons">
					    								<a class="text-link fa fa-eye order-action" title='<%=kkEng.getMsg("common.view")%>' href='<%="ShowOrderDetails.action?orderId="+order.getId()%>'></a>
					    								<a class="text-link fa fa-repeat order-action" title='<%=kkEng.getMsg("common.repeat")%>' href='<%="RepeatOrder.action?orderId="+order.getId()%>'></a>
					    								<a class="text-link fa fa-truck order-actio" title='<%=kkEng.getMsg("common.track")%>'></a>	
													<%if (enableInvoice) {%>	
														<%if (kkEng.isPortlet()){ %>
															<a title='<%=kkEng.getMsg("common.invoice")%>' class="text-link fa fa-file-pdf-o order-action" href='<%="DownloadInvoicePortlet.action?orderId="+order.getId()%>'></a>
														<%} else {%>
															<a title='<%=kkEng.getMsg("common.invoice")%>' class="text-link fa fa-file-pdf-o order-action" href='<%="DownloadInvoice.action?orderId="+order.getId()%>'></a>
														<%}%>
													<% } %>
												</td>
			    							</tr>
				    						<%if (order.getOrderProducts() != null && order.getOrderProducts().length > 0){ %>
					    						<tr>
					    							<td colspan="8">	    					
							    						 <table>
									    					<thead>
									    						<tr>
									    							<td class="wide-col"><kk:msg  key="common.item"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.quantity"/></td>
									    							<td class="narrow-col right"><kk:msg  key="common.total"/></td>		    						
									    						</tr>
									    					</thead>
									    					<tbody>				    						
							    							<% for (int j = 0; j < order.getOrderProducts().length; j++){ %>	
							    								<% com.konakart.appif.OrderProductIf orderProd = order.getOrderProducts()[j];%>
									    						<tr>
									    							<td><a class="text-link" href='<%="SelectProd.action?prodId="+orderProd.getProductId()%>'><%=orderProd.getName()%></a></td>
									    							<td class="right"><%=orderProd.getQuantity()%></td>
									    							<%if (kkEng.displayPriceWithTax()) {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceIncTax(),order.getCurrencyCode())%></td>
																	<%} else {%>
																		<td class="right"><%=kkEng.formatPrice(orderProd.getFinalPriceExTax(),order.getCurrencyCode())%></td>
																	<%}%>	
									    						</tr>
							    							<% } %>
									    					</tbody>	

									    					<tfoot>
										    					<tr class="total-amount">
										    						<td><kk:msg  key="common.total"/>: </td>
										    						<td></td>
										    						<td class="all-orders-total"><%=kkEng.formatPrice(order.getTotalIncTax(),order.getCurrencyCode())%></td>										    				
									    						</tr>  
									    					</tfoot>     		

									    				</table>
								    				</td>
					    						</tr>
					    					<% } %>		    							    							    							    				
					    				</table> 
					    			</div>																	
				    			<% } %>
						</div>

	    				<div class="order-navigation">
	    					<div class="order-navigation-left">
		    					<span class="number-of-items navigation-element"><%=orderMgr.getCurrentOffset() + 1%>-<%=orderMgr.getNumberOfOrders() + orderMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=orderMgr.getTotalNumberOfOrders()%></span>				    					
			    				<span class="separator"></span>
							</div>
							<div class="order-navigation-right">
								<kk:pageSize action="OrderPageSize.action" name="numOrders" sizes="5,10,20,30,50" maxNum="<%=orderMgr.getPageSize()%>"/>
			    				<span class="separator"></span>
	    						<kk:paging pageList="<%=orderMgr.getPageList()%>" currentPage="<%=orderMgr.getCurrentPage()%>" showBack="<%=orderMgr.getShowBack()%>" showNext="<%=orderMgr.getShowNext()%>" action="NavigateAllOrders" timestamp="0"></kk:paging>
							</div>
						</div>
					<%}%>
					<div class="form-buttons-wide">
						<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
					</div>
		    	</div>

