
<%@include file="Taglibs.jsp" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
<% com.konakart.app.EngineConfig engineConfig = new com.konakart.app.EngineConfig(); %>
<% com.konakart.appif.KKEngIf engine = new com.konakart.app.KKEng(engineConfig); %>

<%for (int i = 0; i < catMgr.getCats().length; i++) {%>
   <%com.konakart.appif.CategoryIf cat = catMgr.getCats()[i]; %>
   <% com.konakart.appif.ProductsIf products = engine.getProductsPerCategory(null, null, cat.getId(), true, -1); %>
   <kk:carousel prods="<%=products.getProductArray()%>" title="<%=cat.getName()%>" width="<%=kkEng.getContentClass()%>"/>
 <% } %>


