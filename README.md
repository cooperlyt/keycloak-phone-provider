# Keycloak (Quarkus 21.0.1)  Phone Provider
![Build Status](https://github.com/cooperlyt/keycloak-phone-provider/actions/workflows/compile-and-liveness-check.yml/badge.svg)

 + Phone support like e-mail 
 + OTP by phone
 + Login by phone
 + Register with phone
 + Authentication by phone
 + Reset password by phone

sms
voice
phone one key login

With this provider you can **enforce authentication policies based on a verification token sent to users' mobile phones**.
Currently, there are implementations of Twilio, TotalVoice, and YunTongXun SMS sender services. That said, more
services can be added with ease due to the modularity.  In fact, nothing would stop you from implementing a 
sender of TTS calls or WhatsApp messages. 

This is what you can do for now:
  + Check ownership of a phone number (Forms and Rest API)
  + Use SMS as second factor in 2FA method (Browser flow)
  + Login by phone (Browser flow)
  + Reset Password by phone
  + Authentication by phone (Rest API)
  + Authentication everybody by phone, auto create user on Grant(Rest API)
  + Register with phone 
  + Register only phone (username is phone number)
  + Register add user attribute with redirect_uri params


## Compatibility

This was initially developed using 20.0.1 version of Quarkus Keycloak as baseline.  Wildfily keycloak is not support 
anymore, and I did not test user storage beyond Kerberos or LDAP. I may try to help you but I cannot guarantee.

## Usage

### **Installing:**

+ Docker
  1. docker image is [coopersoft/keycloak:21.0.1_phone-2.2.2](https://hub.docker.com/repository/docker/coopersoft/keycloak)
  2. for examples  [docker-compose.yml](https://raw.githubusercontent.com/cooper-lyt/keycloak-phone-provider/master/examples/docker-compose.yml)
  3. run as `docker-compose up` , docker-compose is required!

If you want to build the project, simply run  `examples/docker-build.sh` after cloning the repository.
 2.2.1 is build on JAVA 17, Maven 3.8.6
  + `keycloak-phone-provide`
    main 
  + `keycloak-phone-provide.resources`
    theme
  + `keycloak-sms-provider-dummy`
    test message will print to console.

  sms service provider, choose one.
  `keycloak-sms-provider-aws-sns`
  `keycloak-sms-provider-totalvoice`
  `keycloak-sms-provider-twilio`
  `keycloak-sms-provider-cloopen`
  `keycloak-sms-provider-yunxin`
  `keycloak-sms-provider-aliyun`
  `keycloak-sms-provider-tencent`
+ Local
  1. local keycloak installed: copy the `target\providers` to keycloak home directory
  2. kc.[sh|bat] build
  3. Start Keycloak.

+ Cli params
```shell
  kc.[sh|bat] start \
    --spi-phone-default-service=[dummy|aws|aliyun|cloopen| ...]  # Which sms provider 
    --spi-phone-default-token-expires-in=60  # sms expires ,default 60 second
    --spi-phone-default-hour-maximum=3 # How many send sms count in one hour. 
    --spi-phone-default-[$realm-]duplicate-phone=false # allow one phone register multi user
    --spi-phone-default-[$realm-]number-regx=^\+?\d+$
    
    ...  # provider param refer provider`s readme.md
```

### **Theme**

You will need to change the realm login theme to 'phone'.

You can create a customized theme base on 'phone'.

```
  parent=phone
```

### **Phone registration support**

Under Authentication > Flows:
+ Create flows from registration:
  Copy the 'Registration' flow to 'Registration with phone' flow.

+ Replace 'Registration User Creation' to 'Registration Phone User Creation'

+ (Optional) Click Settings on 'Registration Phone User Creation', config it;

+ (Optional) Verify Phone
  Click on 'Registration with phone registration Form >Add 'Phone validation' if you want to verify phone.

+ (Optional)Read query parameter add to user attribute:
  Click on 'Registration with phone registration Form > Actions > Add execution' on the 'Query Parameter Reader' line
  Click on 'Registration with phone registration Form > Actions > configure' add accept param name in to

+ (Optional)Hidden password field:
  Delete or disable 'Password Validation'.

+ (Optional) if not any user profile: 
  Delete or disable 'Profile Validation'

Set All add item as Required.

Set Bind 'Registration with phone' to 'Registration flow'

Under Realm Settings > Themes
Set Login Theme as 'phone'

Tip:
  if Realm parameter 'Email as username' is true, then config 'Phone number as username' and 'hide email' is invalid!
  if parameter 'duplicate-phone' is true then 'Phone number as username' is invalid!

![Registration with phone](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/a0.png)


Registration URL:
```
http://<domain>/realms/<realm name>/protocol/openid-connect/registrations?client_id=<client id>&response_type=code&scope=openid%20email&redirect_uri=<redirect_uri>
```
### **Login by phone**
Under Authentication > Flows:
+ Copy the 'Browser' flow to 'Browser with phone' flow
+ Replace 'Username Password Form' to 'Phone Username Password Form' 

Under Realm Settings > Themes
Set Login Theme as 'phone'

Set Bind 'Browser with phone' to 'Browser flow'

![Login By phone](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/e0.jpg)


### **OTP by Phone**

Two user attributes are going to be used by this provider: _phoneNumberVerified_ (bool) and _phoneNumber_ (str). Many
users can have the same _phoneNumber_, but only one of them will have _phoneNumberVerified_ = true at the end of a
verification process. This accommodates the use case of pre-paid numbers that get recycled if inactive for too much time.


  in Authentication page, copy the browser flow and replace OTP to  `OTP Over SMS` . Don't forget to bind this flow copy as the de facto browser flow.
  Finally, register the required actions `Update Phone Number` and `Configure OTP over SMS` in the Required Actions tab.

![OTP](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/b0.jpg)

### **Only use phone login or get Access token use endpoints:**

Under Authentication > Flows:
 + Copy the 'Direct Grant' flow to 'Direct grant with phone' flow
 + Click on 'Add step' on the 'Provide Phone Number' line
 + Click on 'Add step' on the 'Provide Verification Code' line
 + Delete or disable other
 + Set both of 'Provide Phone Number' and 'Provide Verification Code' to 'REQUIRED'

Under 'Clients > $YOUR_CLIENT > Advanced > Authentication Flow Overrides' 
Set Bind 'Direct Grant Flow' to 'Direct grant with phone' 

![Setting](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/c0.jpg)

### **Everybody phone number( if not exists create user by phone number) get Access token use endpoints:**

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


## **Reset credential**

Under Authentication > Flows:
+ Copy the 'Reset credentials' flow to 'Reset credentials with phone' flow
+ Click on 'Add step' on the 'Rest Credential With Phone' line
+ Click on 'Add step' on the 'Send Rest Email If Not Phone' line
+ Delete or disable other
+ set 'Send Rest Email If Not Phone' to 'Conditional'
+ Set both of 'Rest Credential With Phone' and 'Reset Password' to 'REQUIRED'

Set Bind 'Reset credentials with phone' to 'Reset credentials flow'

![Authentication setting](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/d0.jpg)
 
**Phone one key longin**
  Testing , coming soon!


## Thanks
Some code written is based on existing ones in these two projects: [keycloak-sms-provider](https://github.com/mths0x5f/keycloak-sms-provider)
and [keycloak-phone-authenticator](https://github.com/FX-HAO/keycloak-phone-authenticator). Certainly I would have many problems
coding all those providers blindly. Thank you!
