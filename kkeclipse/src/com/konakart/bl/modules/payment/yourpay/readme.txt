YourPay Setup
=============

Using your YourPay merchant account set up Fraud settings as required, eg.

* Add/change credit card numbers to block  
* Add/change names to block  
* Add/change IP/Class C addresses to block  
* Add/change domain names to block  
* Set maximum purchase amount  
* Set lockout times  

Customize YourPay Connect Settings:

Order submission form: 
  (This must match the value you set up in the KonaKart Admin App for the YourPay module)
  (If these don't match you will get Error 1002) 
  https://www.yourserver.com/konakart/EditCartSubmit.do

Confirmation URL:
  Leave blank

Failure Page:
  Leave blank



In the KonaKart Admin App
=========================

Install the YourPay payment module

Define your storename (or storenumber)

Set the referrer URL.   See above.  This must match the "Order submission form" that you
have to set in the YourPay merchant interface.

Choose the required test and debug modes.

Choose whether or not to request the credit card CCV field from the user.

Choose to enable or disable the module.

Choose the sort-order (the order it is shown in the application).

If you have changed values, you should either refresh the caches or restart tomcat.
(To refresh the caches go to "Tools >> Refresh Caches" then click on Refresh).


