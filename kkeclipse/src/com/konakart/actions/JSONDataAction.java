package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.appif.CategoryIf;
import com.opensymphony.xwork2.Action;

public class JSONDataAction extends BaseAction {

	private CategoryIf[] cats;

	private String jsonRequestdata;

	public JSONDataAction() {
	}

	public String execute() {

		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		try {
			
			System.out.println("jsonRequestdata: "+jsonRequestdata);
			KKAppEng kkAppEng = this.getKKAppEng(request, response);

			cats = kkAppEng.getCategoryMgr().getCats();

		} catch (Exception e) {
			return super.handleException(request, e);
		}

		return Action.SUCCESS;
	}

	public CategoryIf[] getCats() {
		return cats;
	}

	public void setCats(CategoryIf[] cats) {
		this.cats = cats;
	}

	public String getJsonRequestdata() {
		return jsonRequestdata;
	}

	public void setJsonRequestdata(String jsonRequestdata) {
		this.jsonRequestdata = jsonRequestdata;
	}
}
