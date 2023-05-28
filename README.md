# Keycloak (Quarkus 21.x.x)  Phone Provider
![Build Status](https://github.com/cooperlyt/keycloak-phone-provider/actions/workflows/compile-and-liveness-check.yml/badge.svg)
![ci](https://github.com/cooperlyt/keycloak-phone-provider/actions/workflows/ci-keycloak20.yml/badge.svg)
![ci](https://github.com/cooperlyt/keycloak-phone-provider/actions/workflows/ci-keycloak21.yml/badge.svg)
 + Phone support like e-mail
 + One Time Password (OTP) by phone
 + Login by phone
 + Register with phone
 + Authentication by phone
 + Reset password by phone

sms
voice
phone one key login

With this provider you can **enforce authentication policies based on a verification token sent to users' mobile phones**.
Currently, there are implementations for:

+ Aliyun
+ AWS SNS
+ Cloopen
+ Tencent
+ TotalVoice
+ Twilio,
+ YunTongXun SMS

More services can be added with ease due to the modularity of the code.  In fact, nothing would stop you from implementing a
sender of TTS calls or WhatsApp messages.

This is what you can do for now:
  + Check ownership of a phone number (Forms and Rest API)
  + Use SMS as second factor in 2FA method (Browser flow)
  + Login by phone (Browser flow)
  + Reset Password by phone
  + Authentication by phone (Rest API)
  + Authenticate everybody by phone, auto create user on Grant (Rest API)
  + Register with phone
  + Register only phone (username is phone number)
  + Register add user attribute with `redirect_uri` params

## Features

### New in Version 2.3.3
+ Add `Condition - phone provided` [#46](https://github.com/cooperlyt/keycloak-phone-provider/issues/46)

### New in Version 2.3.2
+ fix phone login form display error!

### New in Version 2.3.1
+ Canonicalize phone numbers using [Google's libphonenumbers](https://github.com/google/libphonenumber) 
+ Valid phone number using [Google's libphonenumbers](https://github.com/google/libphonenumber)
+ Cli param `number-regx` rename to `number-regex`, and match regex at after canonicalize phone number
+ Fixed Bug [#40 OTP Cookie bypass](https://github.com/cooperlyt/keycloak-phone-provider/issues/40)
+ Remove OTP setting `Cookie Max Age` and add cli param otp-expires
+ Refactor OTP , only use Credential's phone number (The certificate's phone number comes from Required action `Configure OTP over SMS` or setting `Create OTP Credential` in user registration  ), Regardless of the user's phone number
+ Cli param `hour-maximum` rename to `target-hour-maximum`
+ Add cli param `source-hour-maximum`

Migration: 
+ Set cli param `canonicalize-phone-numbers` is "" or `compatible` is true , because in old user data phone number is not canonicalize.
+ Change `number-regx` to `number-regex` and change regex match after canonicalize phone number
    

### New in Version 2.2.2
+ fix phone number as username bug [#24](https://github.com/cooperlyt/keycloak-phone-provider/issues/24)



## Compatibility

This was initially developed using Quarkus Keycloak as baseline.  Wildfily keycloak is not supported
anymore and I did not test user storage beyond Kerberos or LDAP. I may try to help you but I cannot guarantee.

## Usage

### **Installing:**

+ Docker
  1. docker image is [coopersoft/keycloak:21.0.2_phone-2.3.3](https://hub.docker.com/repository/docker/coopersoft/keycloak)
  2. for examples  [docker-compose.yml](https://raw.githubusercontent.com/cooper-lyt/keycloak-phone-provider/master/examples/docker-compose.yml)
  3. run as `docker-compose up` , [docker-compose](https://docs.docker.com/compose/) is required!

If you want to build the project, simply run  `examples/docker-build.sh` after cloning the repository.

  + `keycloak-phone-provide`  
    main
    
  + `keycloak-phone-provide.resources`  
    theme

  + `keycloak-sms-provider-dummy`  
    test message will print to console.

    For sms service provider, choose one of:  
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
    --spi-phone-default-source-hour-maximum=10 # How many send from ip address sms count in one hour, Zero is no limit. default 10 
    --spi-phone-default-target-hour-maximum=3 # How many send to phone number sms count in one hour, Zero is no limit, default 3 
    --spi-phone-default-[$realm-]duplicate-phone=false # allow one phone register multi user, default: false
    --spi-phone-default-[$realm-]default-number-regex=^\+?\d+$ #Notice: will match after canonicalize number. eg: INTERNATIONAL: +41 44 668 18 00 , NATIONAL: 044 668 18 00 , E164: +41446681800
    --spi-phone-default-[$realm-]valid-phone=true # valid phone number, default: true
    #whether to parse user-supplied phone numbers and put into canonical International E.163 format.  _Required for proper duplicate phone number detection_
    --spi-phone-default-[$realm-]canonicalize-phone-numbers=E164 #[E164,INTERNATIONAL,NATIONAL,RFC3966], default: "" un-canonicalize;  
    #a default region to be used when parsing user-supplied phone numbers. Lookup codes at https://www.unicode.org/cldr/cldr-aux/charts/30/supplemental/territory_information.html
    --spi-phone-default-[$realm-]phone-default-region=US #default: use realm setting's default Locate; 
    #if compatible is true then search user will be use all format phone number 
    --spi-phone-default-[$realm-]compatible=false #default: false
    #Prevent 2FA from always happening for a period of time
    --spi-phone-default-[$realm-]otp-expires=3600 #default: 60 * 60; 1 hour

    ...  # provider param refer provider`s readme.md
```

### **Theme**

You will need to change the realm login theme to `phone`.

You can create a customized theme base on `phone`.

```
  parent=phone
```

### **Phone registration support**

Two user attributes are going to be used by this provider: `phoneNumberVerified` (bool) and `phoneNumber` (str). Multiple
users can have the same `phoneNumber`, but only one of them will have `phoneNumberVerified` = `true` at the end of a
verification process. This accommodates the use case of pre-paid numbers that get recycled if inactive for too much time.

Under `Authentication` > `Flows`:

+ Copy the `Registration` flow to `Registration with phone` flow through the menu button on the right of the `registration` flow

+ Replace `Registration User Creation` with `Registration Phone User Creation`

+ (Optional) Click on settings for `Registration Phone User Creation` to configure it

+ (Optional) To enable phone verification, click on `Registration with phone registration Form` >`Add` `Phone validation` if you want to verify phone.

+ (Optional) Read query parameter add to user attribute:  
  Click on `Registration with phone registration Form` > `Actions` > `Add execution` on the `Query Parameter Reader` line  
  Click on `Registration with phone registration Form` > `Actions` > `configure` add accept param name in to  

+ (Optional) Hidden password field:  
  Delete or disable `Password Validation`.

+ (Optional) if not any user profile:  
  Delete or disable `Profile Validation`

Set all added items as `Required`.

On the `Authentication` page, bind `Registration with phone` to `Registration flow` and select it to be `Required`.

Under `Realm Settings` > `Themes`
Set `Login Theme` to `phone`

Tip:
  If Realm parameter `Email as username` is true, then config `Phone number as username` and `hide email` is invalid!  
  If parameter `duplicate-phone` is true then `Phone number as username` is invalid!

![Registration with phone](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/a0.png)


Registration URL:
```
http://<domain>/realms/<realm name>/protocol/openid-connect/registrations?client_id=<client id>&response_type=code&scope=openid%20email&redirect_uri=<redirect_uri>
```
### **Login by phone**
Under `Authentication` > `Flows`:
+ Copy the `Browser` flow to `Browser with phone` flow
+ Replace `Username Password Form` with `Phone Username Password Form`
+ Click on the settings icon next to `Phone Username Password Form` to configure.

Under `Realm Settings` > `Themes`
Set Login Theme as `phone`

Set Bind `Browser with phone` to `Browser flow`
On the `Authentication` page, bind `Browser with phone` to `Browser flow`

![Login By phone](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/e0.jpg)


### **2FA by Phone OTP**


 Phone OTP uses OTP Credential's phone number,Different from the user's phone number, Credential's phone number come from required actions `Configure OTP over SMS`, Unless the `Create OTP Credential` is enabled on user registration flow.


  On Authentication page, copy the browser flow and replace `OTP` with  `OTP Over SMS` . Don't forget to bind this flow copy as the de facto browser flow.
  Finally, Enable the required actions `Configure OTP over SMS` in the Required Actions tab.



![OTP](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/b0.jpg)

### **Only use phone login or get Access token use endpoints:**

Under `Authentication` > `Flows`:
 + Copy the `Direct Grant` flow to `Direct grant with phone` flow
 + Click on `Add step` on the `Provide Phone Number` line
 + Click on `Add step` on the `Provide Verification Code` line
 + Delete or disable other
 + Set both of `Provide Phone Number` and `Provide Verification Code` to `REQUIRED`

Under `Clients` > `$YOUR_CLIENT` > `Advanced ` > `Authentication Flow Overrides`
Bind `Direct Grant Flow` to `Direct grant with phone`

![Setting](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/c0.jpg)

Either Phone/Otp or Username/Password :
![Setting](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/f0.png)

[Android client example](https://github.com/cooperlyt/KeycloakPhoneClient)
### **Everybody phone number( if not exists create user by phone number) get Access token use endpoints:**

Under `Authentication` > `Flows`:
 + Copy the `Direct Grant` flow to `Direct grant everybody with phone` flow
 + Click on `Actions` > `Add step` on the `Authentication Everybody By Phone` line and move to first
 + Delete or disable other
 + Set `Authentication Everybody By Phone` to `REQUIRED`

Under `Clients` > `$YOUR_CLIENT` > `Advanced` > `Authentication Flow Overrides`
Set Direct Grant Flow to `Direct grant everybody with phone`

**About the API endpoints:**

You'll get 2 extra endpoints that are useful to do the verification from a custom application.

+ `GET /realms/{realmName}/sms/verification-code?phoneNumber=+5534990001234` (To request a number verification. No auth required.)
+ `POST /realms/{realmName}/sms/verification-code?phoneNumber=+5534990001234&code=123456` (To verify the process. User must be authenticated.)

You'll get 2 extra endpoints that are useful to do the access token from a custom application.
+ `GET /realms/{realmName}/sms/authentication-code?phoneNumber=+5534990001234` (To request a number verification. No auth required.)
+ `POST /realms/{realmName}/protocol/openid-connect/token`
  `Content-Type: application/x-www-form-urlencoded`
  `grant_type=password&phone_number=$PHONE_NUMBER&code=$VERIFICATION_CODE&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRECT`


And then use Verification Code authentication flow with the code to obtain an access code.


## **Reset Credentials**

Under `Authentication` > `Flows`:
+ Copy the `Reset credentials` flow to `Reset credentials with phone` flow
+ Click on `Add step` on the `Rest Credential With Phone` line
+ Click on `Add step` on the `Send Rest Email If Not Phone` line
+ Delete or disable other
+ set `Send Rest Email If Not Phone` to `Conditional`
+ Set both of `Rest Credential With Phone` and `Reset Password` to `REQUIRED`

Set Bind `Reset credentials with phone` to `Reset credentials flow`

![Authentication setting](https://github.com/cooper-lyt/keycloak-phone-provider/raw/master/examples/document/d0.jpg)

## **Conditional**
 `Condition - phone provided` 

## **Required Action**
+ `Update Phone Number` update user's phone number on next login.
+ `Configure OTP over SMS` update OTP Credential's phone number on next login.



**Phone one key login**
  Testing , coming soon!


## Thanks
Some code written is based on existing ones in these two projects: [keycloak-sms-provider](https://github.com/mths0x5f/keycloak-sms-provider)
and [keycloak-phone-authenticator](https://github.com/FX-HAO/keycloak-phone-authenticator). Certainly I would have many problems
coding all those providers blindly. Thank you!
