<%@include file="Taglibs.jsp" %>
<%@ page import="java.util.List" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
<% com.konakart.app.EngineConfig engineConfig = new com.konakart.app.EngineConfig(); %>
<% com.konakart.appif.KKEngIf engine = new com.konakart.app.KKEng(engineConfig); %>

<% int noOfCategories = catMgr.getCats().length; %>
<% com.konakart.appif.CategoryIf[] categories = catMgr.getCats(); %>
<% for (int i = 0; i < noOfCategories; i++) {%>
   <% com.konakart.appif.CategoryIf cat = categories[i]; %>
   <% com.konakart.appif.ProductsIf products = engine.getProductsPerCategory(null, null, cat.getId(), true, -1); %>
   <kk:carousel prods="<%=products.getProductArray()%>" title="<%=cat.getName()%>" width="<%=kkEng.getContentClass()%>"/>
 <% } %>
 
 <%-- Code added to access invisible categories data --%>
 <% List<com.konakart.appif.CategoryIf> cats = com.konakart.app.GetCategoryTree.getAllInvisibleCategories();%>
 <% for (com.konakart.appif.CategoryIf cat : cats) { %>
     <%  System.out.println("id:"+cat.getId()+" image:"+cat.getImage()+" name: "+cat.getName()); %>
 <% } %>

