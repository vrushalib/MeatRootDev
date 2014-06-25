GlobalCollect Payment Gateway
=============================

To use the GlobalCollect payment gateway you will need to set up a merchant account 
with GlobalCollect and set up the module in KonaKart as follows:

1) Install the GlobalCollect module in the KonaKart Admin App

2) You can enable/disable the GlobalCollect gateway by setting the Status checkbox

3) Optionally define a zone where the GlobalCollect gateway can be used.  Leave as 
"--none--" if you want GlobalCollect to be availabe in all zones.

4) Request URL.  By default this is set to https://ps.gcsip.nl/wdl/wdl when you 
install the module.  This is the test server.   When you wish to switch over to the 
production server you should change this to https://ps.gcsip.com/wdl/wdl

5) Response URL.  By default this is set to 
http://host:port/konakart/GlobalCollectCardResponse.do.  "host:port" are automatically
substitued at runtime - but you can also define the response URL without using 
this syntax.   The response URL is used by GlobalCollect to communicate payment
status back to KonaKart so this must be a URL that is visible from GlobalCollect 
(eg. not a localhost address).

6) Merchant Account Id.   You will need to establish a Merchant Account with 
GlobalCollect.  They will provide you with a Merchant Account Id for this field.

7) Server IP Address.   Set this to the IP address of the server that will be used
to send your payment requests to GlobalCollect.   You will have to set up your
GlobalCollect account with these addresses as they use these to authenticate 
requests.

8) Supported Product Ids.  Define the payment methods you want to make available.  
Use the payment product Ids in a comma (or space, or semi-colon) -separated list.  
The order you specify these is significant as it defines the order that the options 
are presented in the default storefront.  The payment methods that will be returned 
to the stroefront will be those supported on your Merchant account and those also 
defined in your definition in the Konakart Admin App.  Payment Products must be 
defined in both places for them to become accessible to the storefront.


Notes:

1) Styling

You can add your own styling to the GlobalCollect pages by hosting your own CSS and 
images.  Refer to the GlobalCollect documentation under the section "Style sheets 
and GlobalCollect hosted sites" for more details.

2) HostedMerchant Link

The KonaKart module has been designed to work with the HostedMerchant Link interface
of Global Collect.   The module will have to be modified in order to work with the
other Global Collect interfaces.  Full source code is provided as with all modules.

