Authorize.Net Set-up
====================

KonaKart supports the following AuthorizeNet integration types:

1) Advanced Integration Method (AIM)
2) Direct Post Method (DPM)
3) Payment Tokenization (CIM)
4) Automated Recurring Billing™ (ARB)

AIM and DPM allow a customer to enter credit card details for payment. The main difference is that AIM transmits the customer's credit card details to your server (where KonaKart is installed) where they are then sent to AuthorizeNet. They are never saved but they do pass through your server. On the other hand DPM works in such a way that the credit card details are posted directly to AuthorizeNet from the customer's browser and so never enter your server. The fact that you never come into contact with the customer's credit card details, simplifies the PCI compliance process. Where possible we encourage you to use DPM.

CIM allows a customer to store credit card details and to use the stored details for payment so that he doesn't have to re-enter the credit card details every time. It has been implemented in hosted mode so that the customer's credit card details never enter your server. 

The KonaKart storefront may be configured in three different ways:

1) AIM only
2) DPM only
3) DPM + CIM

Option 3) allows a customer to manage his credit cards from the "My Account" page. The credit card entry page allows the customer to choose from a list of stored credit cards or to pay by entering credit card details.

When configuring the AuthorizeNet module through the Admin App you may decide (through a radio button) whether to enable DPM (AIM is enabled by default). If you decide to use DPM you have to supply some extra configuration parameters which are not required for AIM.

MD5 Hash Key  :  If the MD5 Hash Key has been explicitly set in the AuthorizeNet merchant interface then this value must match the one set through the merchant interface under Account >> MD5 Hash . Otherwise it should be left empty.

Direct Post Relay URL  :  This URL must be accessible from the internet. It should be in the form http://host:port/konakart/AuthNetCallback.action where the host and port are set so that AuthorizeNet can call AuthNetCallback.action .

Direct Post Callback username and password  :  These credentials should be valid credentials that allow the callback from AuthorizeNet to log into the KonaKart eCommerce server engine. Note that the credentials do not need to be those of an administrator.

If you've chosen DPM, you may also activate CIM through a radio button. CIM requires the configuration of an extra parameter which is the CIM web service URL where XML messages are sent.