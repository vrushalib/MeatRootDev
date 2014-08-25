package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

/**
 * Used to implement suggested search
 */
public class SuggestedArea extends BaseAction
{
    private static final long serialVersionUID = 1L;
    
    private String pincode;
    
    private String emailId;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        try
        {
        	String pinCode = getPincode();
        	String emailId = getEmailId();
        	System.out.println(pinCode+":"+emailId);
            if ( pinCode != null && !pinCode.equals("") && emailId != null && !emailId.equals(""))
            {
        		com.konakart.db.KKBasePeer.executeStatement("insert into suggested_areas (emailid, pincode) values ('"+emailId+"', '"+pinCode+"');", org.apache.torque.Torque.getConnection("store1", "root", "pass123"));
                return SUCCESS;
            }

            return SUCCESS;

        } catch (Exception e)
        {
        	e.printStackTrace();
            return super.handleException(request, e);
        }
    }
    
	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}


}
