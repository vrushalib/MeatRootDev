Installation instructions
-------------------------

1) 
Copy konakart_payment_elink.jar to KonaKart\webapps\konakart\WEB-INF\lib and to KonaKart\webapps\konakartadmin\WEB-INF\lib.

2) 
Edit KonaKart\webapps\konakartadmin\WEB-INF\classes\konakartadmin.properties to add the payment module. Add the name “Elink” to the “konakart.modules.payment” property.

e.g.
konakart.modules.payment=Paypal Chronopay Epaybg Cod Worldpay Authorizenet Usaepay Yourpay Payjunction Bluepay Elink

3)
Re-start the servlet engine (i.e. Tomcat) and you will now see eLink in the list of payment modules in the Admin App. Install the module and configure it with your account number and password.

4)
Using the Admin App, refresh the caches (Tools>>Refresh Caches)

5)
The eLink payment gateway should now be available when checking out using the eCommerce application.
