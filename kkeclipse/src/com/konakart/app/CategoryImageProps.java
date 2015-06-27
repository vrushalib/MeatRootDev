package com.konakart.app;

import flexjson.JSONSerializer;

public class CategoryImageProps {
	String cU;
	String bLU;
	String bRU;
	String bLL;
	String bRL;
	
	public CategoryImageProps() {
	}

	
	
	/**
	 * @return the cU
	 */
	public String getcU() {
		return cU;
	}



	/**
	 * @param cU the cU to set
	 */
	public void setcU(String cU) {
		this.cU = cU;
	}



	/**
	 * @return the bLU
	 */
	public String getbLU() {
		return bLU;
	}



	/**
	 * @param bLU the bLU to set
	 */
	public void setbLU(String bLU) {
		this.bLU = bLU;
	}



	/**
	 * @return the bRU
	 */
	public String getbRU() {
		return bRU;
	}



	/**
	 * @param bRU the bRU to set
	 */
	public void setbRU(String bRU) {
		this.bRU = bRU;
	}



	/**
	 * @return the bLL
	 */
	public String getbLL() {
		return bLL;
	}



	/**
	 * @param bLL the bLL to set
	 */
	public void setbLL(String bLL) {
		this.bLL = bLL;
	}



	/**
	 * @return the bRL
	 */
	public String getbRL() {
		return bRL;
	}



	/**
	 * @param bRL the bRL to set
	 */
	public void setbRL(String bRL) {
		this.bRL = bRL;
	}



	public static void main(String[] args) {
		CategoryImageProps ci = new CategoryImageProps();
		System.out.println(new JSONSerializer().serialize(ci));
	}
	
}
