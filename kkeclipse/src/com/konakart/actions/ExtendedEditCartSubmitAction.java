package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.json.BasketJson;
import com.konakart.al.json.OptionJson;
import com.konakart.al.json.WishListJson;
import com.konakart.appif.BasketIf;
import com.konakart.appif.OptionIf;

/**
 * This action class would be called when User edits cart on the Shopping cart pop-up
 *
 */
public class ExtendedEditCartSubmitAction extends EditCartSubmitAction {

	private static final long serialVersionUID = 1L;
	
	protected int prodId = -1;

    // When not null, need to forward to prod details page to set options
    protected String redirectURL = null;

    // Array of basket items
    protected BasketJson[] items = null;

    // Array of wish list items
    protected WishListJson[] wlItems = null;

    // Formatted Basket total
    protected String basketTotal = "";

    // Number of items
    protected int numberOfItems = 0;

    // Base path for images
    protected String imgBase;

    // Message catalog strings
    protected String shoppingCartMsg;

    protected String checkoutMsg;

    protected String subtotalMsg;

    protected String quantityMsg;
    
    private String xsrf_token;
    
    protected void setMsgs(KKAppEng kkAppEng)
    {
        // Message catalog strings
        shoppingCartMsg = kkAppEng.getMsg("cart.tile.shoppingcart");
        checkoutMsg = kkAppEng.getMsg("common.checkout");
        subtotalMsg = kkAppEng.getMsg("common.subtotal");
        quantityMsg = kkAppEng.getMsg("cart.tile.quantity");
    }

	@Override
	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        
		//String result = super.execute();
		try {
			KKAppEng kkAppEng = this.getKKAppEng(request, response);
			
			setImgBase(kkAppEng.getImageBase());
			
			BasketIf[] basketItems = kkAppEng.getCustomerMgr().getCurrentCustomer()
                    .getBasketItems();
            if (basketItems != null)
            {
                items = new BasketJson[basketItems.length];
                for (int i = 0; i < basketItems.length; i++)
                {
                    BasketIf b = basketItems[i];
                    BasketJson bj = new BasketJson();
                    items[i] = bj;
                    if (kkAppEng.displayPriceWithTax())
                    {
                        bj.setFormattedPrice(kkAppEng.formatPrice(b.getFinalPriceIncTax()));
                    } else
                    {
                        bj.setFormattedPrice(kkAppEng.formatPrice(b.getFinalPriceExTax()));
                    }
                    if (b.getOpts() != null && b.getOpts().length > 0)
                    {
                        OptionJson[] optArray = new OptionJson[b.getOpts().length];
                        for (int j = 0; j < b.getOpts().length; j++)
                        {
                            OptionIf opt = b.getOpts()[j];
                            OptionJson optj = new OptionJson();
                            optj.setName(opt.getName());
                            optj.setQuantity(opt.getQuantity());
                            optj.setType(opt.getType());
                            optj.setValue(opt.getValue());
                            if (opt.getCustomerText() != null)
                            {
                                optj.setCustomerText(opt.getCustomerText());
                            }
                            if (opt.getCustomerPrice() != null)
                            {
                                optj.setFormattedCustPrice(kkAppEng.formatPrice(opt
                                        .getCustomerPrice()));
                            }
                            optArray[j] = optj;
                        }
                        bj.setOpts(optArray);
                    }
                    bj.setQuantity(b.getQuantity());
                    bj.setProdId(b.getProductId());
                    if (b.getProduct() != null)
                    {
                        bj.setProdName(b.getProduct().getName());
                        String imgSrc = kkAppEng.getProdImage(b.getProduct(), KKAppEng.IMAGE_TINY);
                        bj.setProdImgSrc(imgSrc);
                    }
                }
            }
			
			basketTotal = kkAppEng.getBasketMgr().getFormattedBasketTotal();
			numberOfItems = kkAppEng.getBasketMgr().getNumberOfItems();
			this.setMsgs(kkAppEng);
			return SUCCESS;
		} catch (Exception e) {
			return super.handleException(request, e);
		}
        
        //return result;
	}

	public int getProdId() {
		return prodId;
	}

	public void setProdId(int prodId) {
		this.prodId = prodId;
	}

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public BasketJson[] getItems() {
		return items;
	}

	public void setItems(BasketJson[] items) {
		this.items = items;
	}

	public WishListJson[] getWlItems() {
		return wlItems;
	}

	public void setWlItems(WishListJson[] wlItems) {
		this.wlItems = wlItems;
	}

	public String getBasketTotal() {
		return basketTotal;
	}

	public void setBasketTotal(String basketTotal) {
		this.basketTotal = basketTotal;
	}

	public int getNumberOfItems() {
		return numberOfItems;
	}

	public void setNumberOfItems(int numberOfItems) {
		this.numberOfItems = numberOfItems;
	}

	public String getImgBase() {
		return imgBase;
	}

	public void setImgBase(String imgBase) {
		this.imgBase = imgBase;
	}

	public String getShoppingCartMsg() {
		return shoppingCartMsg;
	}

	public void setShoppingCartMsg(String shoppingCartMsg) {
		this.shoppingCartMsg = shoppingCartMsg;
	}

	public String getCheckoutMsg() {
		return checkoutMsg;
	}

	public void setCheckoutMsg(String checkoutMsg) {
		this.checkoutMsg = checkoutMsg;
	}

	public String getSubtotalMsg() {
		return subtotalMsg;
	}

	public void setSubtotalMsg(String subtotalMsg) {
		this.subtotalMsg = subtotalMsg;
	}

	public String getQuantityMsg() {
		return quantityMsg;
	}

	public void setQuantityMsg(String quantityMsg) {
		this.quantityMsg = quantityMsg;
	}

	public String getXsrf_token() {
		return xsrf_token;
	}

	public void setXsrf_token(String xsrf_token) {
		this.xsrf_token = xsrf_token;
	}
    
}
