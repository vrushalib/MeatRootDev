package com.konakart.actions.ipn;

enum TransactionStatus{
	SUCCESS("success"), FAILURE("failure"), PENDING("pending");
	
	private final String status;
	
	TransactionStatus(final String status){
		this.status = status;
	}
	
	public String toString(){
		return status;
	}
};