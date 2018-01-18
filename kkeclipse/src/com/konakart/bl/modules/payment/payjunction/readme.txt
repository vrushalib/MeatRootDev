PayJunction Set-up
==================

Note the configuration settings on your PayJunction account and follow the
recommended security best-practices advice.

Install and configure the relevant PayJunction configuration parameters in the 
KonaKart Admin App.

* set the username and password for your Merchant account
* confirm the URL of the payment server is correct
* Enable or disable debug mode (for diagnosing problems)
* Enable or disable PayJunction itself
* Set the sort order for PayJunction (the order it is shown if there are more 
  than one applicable payment gateway modules present).

TLS 1.2
Since June 2016
If using Java 7 and using a Payment Gateway that requires TLS 1.2 you will probably need to add these to CATALIAN_OPTS or JAVA_OPTS:
   -Djdk.tls.client.protocols="TLSv1.2"  -Dhttps.protocols="TLSv1.2"