package com.konakart.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.actions.objects.CartItem;
import com.konakart.actions.objects.ExtendedBasketJson;
import com.konakart.al.KKAppEng;
import com.konakart.al.json.BasketJson;
import com.konakart.al.json.OptionJson;
import com.konakart.al.json.WishListJson;
import com.konakart.appif.BasketIf;
import com.konakart.appif.OptionIf;

public class BulkEditCartAction extends EditCartSubmitAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6733755839288773807L;
	private List<CartItem> cartItems;
	
	protected int prodId = -1;

    // When not null, need to forward to prod details page to set options
    protected String redirectURL = null;

    // Array of basket items
    protected BasketJson[] bItems = null;
    
    protected ExtendedBasketJson[] items = null;

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

    public List<CartItem> getCartItems() {
		return cartItems;
	}

    public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}

	@Override
	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		KKAppEng kkAppEng;
		try {
			int custId;
			kkAppEng = this.getKKAppEng(request, response);

			custId = this.loggedIn(request, response, kkAppEng, null);
            // Force the user to login if configured to do so
            if (custId < 0 && kkAppEng.isForceLogin())
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

			System.out.println("items : " + this.cartItems);
			String result = null;
			String action, id, qtyStr;
			
			if(cartItems != null && !cartItems.isEmpty()) {
				for(CartItem item : cartItems) {
					action = item.getAction();
					id = item.getId().toString();
					qtyStr = item.getQuantity().toString();
					result = performEditOperation(action, id, qtyStr, kkAppEng);
		            if(!result.equals(SUCCESS)) {
		            	return result;
		            }

		            // Update the basket data
		            kkAppEng.getBasketMgr().getBasketItemsPerCustomer();

		            kkAppEng.getNav().set(kkAppEng.getMsg("header.cart.contents"), request);

		            
				}
			}
			/*if (getGoToCheckout().equalsIgnoreCase("true"))
            {
                return "Checkout";
            }
            return "ShowCart";*/
			
			setImgBase(kkAppEng.getImageBase());
			
			BasketIf[] basketItems = kkAppEng.getCustomerMgr().getCurrentCustomer()
                    .getBasketItems();
            if (basketItems != null)
            {
                bItems = new BasketJson[basketItems.length];
                items = new ExtendedBasketJson[basketItems.length];
                for (int i = 0; i < basketItems.length; i++)
                {
                    BasketIf b = basketItems[i];
                    BasketJson bj = new BasketJson();
                    ExtendedBasketJson ebj = new ExtendedBasketJson();
                    bItems[i] = bj;
                    items[i] = ebj;
                    if (kkAppEng.displayPriceWithTax())
                    {
                        bj.setFormattedPrice(kkAppEng.formatPrice(b.getFinalPriceIncTax()));
                        ebj.setFormattedPrice(kkAppEng.formatPrice(b.getFinalPriceIncTax()));
                    } else
                    {
                        bj.setFormattedPrice(kkAppEng.formatPrice(b.getFinalPriceExTax()));
                        ebj.setFormattedPrice(kkAppEng.formatPrice(b.getFinalPriceExTax()));
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
                        ebj.setOpts(optArray);
                    }
                    bj.setQuantity(b.getQuantity());
                    ebj.setQuantity(b.getQuantity());
                    bj.setProdId(b.getProductId());
                    ebj.setProdId(b.getProductId());
                    
                    ebj.setId(b.getId());
                    
                    if (b.getProduct() != null)
                    {
                        bj.setProdName(b.getProduct().getName());
                        ebj.setProdName(b.getProduct().getName());
                        String imgSrc = kkAppEng.getProdImage(b.getProduct(), KKAppEng.IMAGE_TINY);
                        bj.setProdImgSrc(imgSrc);
                        ebj.setProdImgSrc(imgSrc);
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

	public BasketJson[] getbItems() {
		return bItems;
	}

	public void setbItems(BasketJson[] bItems) {
		this.bItems = bItems;
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

	public ExtendedBasketJson[] getItems() {
		return items;
	}

	public void setItems(ExtendedBasketJson[] items) {
		this.items = items;
	}
}
