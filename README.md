# Keycloak Phone Provider

 + Phone support like e-mail 
 + OTP by phone
 + Register with phone
 + Authentication by phone

sms
voice
phone one key login

With this provider you can **enforce authentication policies based on a verification token sent to users' mobile phones**.
Currently, there are implementations of Twilio and TotalVoice and YunTongXun SMS sender services. That said, is nice to note that more
services can be used with ease thankfully for the adopted modularity and in fact, nothing stop you from implementing a 
sender of TTS calls or WhatsApp messages. 

This is what you can do for now:
  + Check ownership of a phone number (Forms and HTTP API)
  + Use SMS as second factor in 2FA method (Browser flow)
  + Reset Password by phone (Testing)
  + Authentication by phone (HTTP API)
  + Authentication everybody by phone, auto create user on Grant(HTTP API)
  + Register with phone 
  + Register only phone (user name is phone number)
  + Register add user attribute with redirect_uri params

  

## Client:
see my project [KeycloakClient](https://github.com/cooper-lyt/KeycloakClient) ,is android client, nothing stop you from implementing other java program.

## Compatibility

This was initially developed using 19.0.1 version of Keycloak as baseline, and I did not test another user storage beyond
the default like Kerberos or LDAP. I may try to help you but I cannot guarantee.

## Usage

**Installing:**

+ Docker
  1. docker image is [coopersoft/keycloak:arm64-19.0.1_phone-2.0](https://hub.docker.com/repository/docker/coopersoft/keycloak)
  2. for examples  [docker-compose.yml](https://raw.githubusercontent.com/cooper-lyt/keycloak-phone-provider/master/examples/docker-compose.yml)
  3. run as `docker-compose up` , docker-compose is required!

If you want to build the project, simply run  `examples/docker-build.sh` after cloning the repository.


+ Local
  1. local keycloak installed: copy the `target\providers` to keycloak home directory
  2. kc.[sh|bat] build
  3. Start Keycloak.

+ Cli params
```shell
  kc.[sh|bat] start \
    --spi-phone-message-service-default-service=[dummy|aws|aliyun|cloopen| ...]  # Which sms provider 
    --spi-phone-message-service-default-token-expires-in=60  # sms expires ,default 60 second
    --spi-phone-message-service-default-hour-maximum=3 # How many send sms count in one hour. 
    ...  # provider param refer provider`s readme.md
```

  


**Phone registration support**

Under Authentication > Flows:
+ Create flows from registration:
  Copy the 'Registration' flow to 'Registration fast by phone' flow.

+ (Optional) Phone number used as username for new user:  
  Delete or disable 'Registration User Creation'.
  Click on 'Registration Fast By Phone Registration Form > Add > Add step' on the 'Registration Phone As Username Creation' line.
  Move this item to first.

+ (Optional)Hidden all other field phone except :   
  Click on 'Registration Fast By Phone Registration Form > Add > Add step' on the 'Registration Least' line

+ Add phone number to profile
  Click on 'Registration Fast By Phone Registration Form > Add > Add step' on the 'Phone Validation' line

+ (Optional)Read query parameter add to user attribute:
  Click on 'Registration Fast By Phone Registration Form > Actions > Add execution' on the 'Query Parameter Reader' line
  Click on 'Registration Fast By Phone Registration Form > Actions > configure' add accept param name in to

+ (Optional)Hidden password field:
  Delete or disable 'Password Validation'.

Set All add item as Required.

Set Bind 'Registration fast by phone' to Registration flow

Under Realm Settings > Themes
Set Login Theme as 'phone'

test:
http://<addr>/auth/realms/<realm name>/protocol/openid-connect/registrations?client_id=<client id>&response_type=code&scope=openid%20email&redirect_uri=<redirect_uri>

**OTP by Phone**

Two user attributes are going to be used by this provider: _phoneNumberVerified_ (bool) and _phoneNumber_ (str). Many
users can have the same _phoneNumber_, but only one of them is getting _phoneNumberVerified_ = true at the end of a
verification process. This accommodates the use case of pre-paid numbers that get recycled if inactive for too much time.


  in Authentication page, copy the browser flow and replace OTP to  `OTP Over SMS` . Don't forget to bind this flow copy as the de facto browser flow.
  Finally, register the required actions `Update Phone Number` and `Configure OTP over SMS` in the Required Actions tab.


**Only use phone login or get Access token use endpoints:**

Under Authentication > Flows:
 + Copy the 'Direct Grant' flow to 'Direct grant with phone' flow
 + Click on 'Add step' on the 'Provide Phone Number' line
 + Click on 'Add step' on the 'Provide Verification Code' line
 + Delete or disable other
 + Set both of 'Provide Phone Number' and 'Provide Verification Code' to 'REQUIRED'

Under 'Clients > $YOUR_CLIENT > Advanced > Authentication Flow Overrides' 
Set Direct Grant Flow to 'Direct grant with phone' 

**Everybody phone number( if not exists create user by phone number) get Access token use endpoints:**

Under Authentication > Flows:
 + Copy the 'Direct Grant' flow to 'Direct grant everybody with phone' flow
 + Click on 'Actions > Add step' on the 'Authentication Everybody By Phone' line and move to first
 + Delete or disable other
 + Set 'Authentication Everybody By Phone' to 'REQUIRED'

Under 'Clients > $YOUR_CLIENT > Advanced > Authentication Flow Overrides' 
Set Direct Grant Flow to 'Direct grant everybody with phone' 

**About the API endpoints:**

You'll get 2 extra endpoints that are useful to do the verification from a custom application.

+ GET /realms/{realmName}/sms/verification-code?phoneNumber=+5534990001234 (To request a number verification. No auth required.)
+ POST /realms/{realmName}/sms/verification-code?phoneNumber=+5534990001234&code=123456 (To verify the process. User must be authenticated.)

You'll get 2 extra endpoints that are useful to do the access token from a custom application.
+ GET /realms/{realmName}/sms/authentication-code?phoneNumber=+5534990001234 (To request a number verification. No auth required.)
+ POST /realms/{realmName}/protocol/openid-connect/token
  Content-Type: application/x-www-form-urlencoded
  grant_type=password&phone_number=$PHONE_NUMBER&code=$VERIFICATION_CODE&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRECT


And then use Verification Code authentication flow with the code to obtain an access code.


**Reset credential**
 Testing , coming soon!
 
**Phone one key longin**
  Testing , coming soon!


## Thanks
Some code written is based on existing ones in these two projects: [keycloak-sms-provider](https://github.com/mths0x5f/keycloak-sms-provider)
and [keycloak-phone-authenticator](https://github.com/FX-HAO/keycloak-phone-authenticator). Certainly I would have many problems
coding all those providers blindly. Thank you!
