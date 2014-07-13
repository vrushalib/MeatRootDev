<%@include file="Taglibs.jsp" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
<% com.konakart.app.EngineConfig engineConfig = new com.konakart.app.EngineConfig(); %>
<% com.konakart.appif.KKEngIf engine = new com.konakart.app.KKEng(engineConfig); %>

<% int noOfCategories = catMgr.getCats().length; %>
<% com.konakart.appif.CategoryIf[] categories = catMgr.getCats(); %>
<% for (int i = 0; i < noOfCategories; i++) {%>
   <% com.konakart.appif.CategoryIf cat = categories[i]; %>
   <% if(cat.getCustom1() == null ){ //category is not invisible. custom1 value for invisible categories is "i".%>
       <% com.konakart.appif.ProductsIf products = engine.getProductsPerCategory(null, null, cat.getId(), true, -1); %>
       <kk:carousel prods="<%=products.getProductArray()%>" title="<%=cat.getName()%>" width="<%=kkEng.getContentClass()%>"/>
   <% } %>
<% } %>
 


