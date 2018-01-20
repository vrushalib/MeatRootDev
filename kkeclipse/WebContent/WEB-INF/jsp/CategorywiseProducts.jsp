<%@include file="Taglibs.jsp" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.CategoryMgr catMgr = kkEng.getCategoryMgr();%>
<% com.konakart.app.EngineConfig engineConfig = new com.konakart.app.EngineConfig(); %>
<% com.konakart.appif.KKEngIf engine = new com.konakart.app.KKEng(engineConfig); %>

 <% int noOfCategories = catMgr.getCats().length; 
 com.konakart.appif.CategoryIf[] categories = catMgr.getCats(); 
 for (int i = 0; i < noOfCategories; i++) {
    com.konakart.appif.CategoryIf cat = categories[i]; 
    if(cat.getCustom1() == null ){ //category is not invisible. custom1 value for invisible categories is "i".
   		 if(cat.getChildren() != null && cat.getChildren().length > 0){ //If a category has a subcategory
   			for(int j = 0; j < cat.getChildren().length ; j++){
   			 	com.konakart.appif.CategoryIf subCat = cat.getChildren()[j]; 
		        com.konakart.appif.ProductsIf products = engine.getProductsPerCategory(null, null, subCat.getId(), true, -1); 
		        com.konakart.appif.ProductIf[] prods = products.getProductArray(); 
		        if(prods != null && prods.length > 0 ){ 
		               prods[0].setCategoryId(subCat.getId()); %>
		       		   <kk:carousel prods="<%=prods%>" title="<%=subCat.getName()%>" width="180" widthSmall="150" breakpointSmall="440"/>
       			<% } 
       		 } 
       	 } 
        else { 
    	    com.konakart.appif.ProductsIf products = engine.getProductsPerCategory(null, null, cat.getId(), true, -1); 
	        com.konakart.appif.ProductIf[] prods = products.getProductArray(); 
	        if(prods != null && prods.length > 0 ){ 
	               prods[0].setCategoryId(cat.getId()); %>
	               <kk:carousel prods="<%=prods%>" title="<%=cat.getName()%>" width="180" widthSmall="150" breakpointSmall="440"/>
   			<% } 
       } 
    } 
 } %>
 


