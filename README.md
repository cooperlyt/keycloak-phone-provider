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

  
Two user attributes are going to be used by this provider: _phoneNumberVerified_ (bool) and _phoneNumber_ (str). Many
users can have the same _phoneNumber_, but only one of them is getting _phoneNumberVerified_ = true at the end of a 
verification process. This accommodates the use case of pre-paid numbers that get recycled if inactive for too much time.

## Compatibility

This was initially developed using 11.0.3 version of Keycloak as baseline, and I did not test another user storage beyond
the default like Kerberos or LDAP. I may try to help you but I cannot guarantee.

## Usage

docker image is [coopersoft/keycloak-phone:11.0.3](https://hub.docker.com/layers/coopersoft/keycloak-phone/11.0.3/images/sha256-cfb890c723a2b9970c59f0bf3e0310499bb6e27e33d685edbc77d992ae15c4c9?context=repo)
for examples  [docker-compose.yml](https://raw.githubusercontent.com/cooper-lyt/keycloak-phone-provider/master/examples/docker-compose.yml)
run as `docker-compose up` , docker-compose is required!


If you want to build the project, simply run `mvn clean package docker:build` after cloning the repository. 
At the end of the goal.
 + local keycloak installed: copt the `target` directory  all jars correctly placed in a WildFly-like folder structure. 
 + docker image build: for examples [run-local.sh](https://github.com/cooper-lyt/keycloak-phone-provider/blob/master/examples/snapshot/run-local.sh) or [run-remote.sh](https://github.com/cooper-lyt/keycloak-phone-provider/blob/master/examples/snapshot/run-remote.sh).


**Installing:**
 
  1. Merge that content with the root folder of Keycloak. You can of course delete the modules of services you won't use,
  like TotalVoice if you're going to use Twilio.
  2. Open your `standalone.xml` (or equivalent) and (i) include the base module and at least one SMS service provider in
  the declaration of modules for keycloak-server subsystem. (ii) Add properties for overriding the defaults of selected
  service provider and expiration time of tokens. (iii) Execute the additional step specified on selected service provider
  module README.md.
  3. Start Keycloak.

i. add modules defs
```xml
<subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
    <web-context>auth</web-context>
    <providers>
        <provider>classpath:${jboss.home.dir}/providers/*</provider>
        <provider>module:keycloak-sms-provider</provider>
        <provider>module:keycloak-sms-provider-dummy</provider>
    </providers>
...
```
ii. set provider and token expiration time
```xml
<spi name="phoneMessageService">
    <provider name="default" enabled="true">
        <properties>
            <property name="service" value="TotalVoice"/>
            <property name="tokenExpiresIn" value="60"/>
        </properties>
    </provider>
</spi>
```

**OTP by Phone**

  in Authentication page, copy the browser flow and add a subflow to the forms, then adding `OTP Over SMS` as a
  new execution. Don't forget to bind this flow copy as the de facto browser flow.
  Finally, register the required actions `Update Phone Number` and `Configure OTP over SMS` in the Required Actions tab.


**Only use phone login or get Access token use endpoints:**

Under Authentication > Flows:
 + Copy the 'Direct Grant' flow to 'Direct grant with phone' flow
 + Click on 'Actions > Add execution' on the 'Provide Phone Number' line
 + Click on 'Actions > Add execution' on the 'Provide Verification Code' line
 + Delete or disable other
 + Set both of 'Provide Phone Number' and 'Provide Verification Code' to 'REQUIRED'

Under 'Clients > $YOUR_CLIENT > Authentication Flow Overrides' or 'Authentication > Bindings' 
Set Direct Grant Flow to 'Direct grant with phone' 

**Everybody phone number( if not exists create user by phone number) get Access token use endpoints:**

Under Authentication > Flows:
 + Copy the 'Direct Grant' flow to 'Direct grant everybody with phone' flow
 + Click on 'Actions > Add execution' on the 'Authentication Everybody By Phone' line
 + Delete or disable other
 + Set 'Authentication Everybody By Phone' to 'REQUIRED'

Under 'Clients > $YOUR_CLIENT > Authentication Flow Overrides' or 'Authentication > Bindings' 
Set Direct Grant Flow to 'Direct grant everybody with phone' 

**Reset credential**
 Testing , coming soon!
 
**Phone one key longin**
  Testing , coming soon!

**Phone registration support**

Under Authentication > Flows:
 + Create flows from registration:
    Copy the 'Registration' flow to 'Registration fast by phone' flow.
 
 + (Optional) Phone number used as username for new user:  
    Delete or disable 'Registration User Creation'.
    Click on 'Registration Fast By Phone Registration Form > Actions > Add execution' on the 'Registration Phone As Username Creation' line.
    Move this item to first.
    
 +  Add phone number to profile
    Click on 'Registration Fast By Phone Registration Form > Actions > Add execution' on the 'Phone Validation' line

 + (Optional)Hidden all other field phone except :   
    Click on 'Registration Fast By Phone Registration Form > Actions > Add execution' on the 'Registration Least' line

 + (Optional)Read query parameter add to user attribute:
        Click on 'Registration Fast By Phone Registration Form > Actions > Add execution' on the 'Query Parameter Reader' line
        Click on 'Registration Fast By Phone Registration Form > Actions > configure' add accept param name in to 

 + (Optional)Hidden password field:
    Delete or disable 'Password Validation'.
    
 Set All add item as Required.

Under Authentication > Bindings
Set Registration Flow to 'Registration fast by phone' 

Under Realm Settings > Themes
Set Login Theme as 'phone'

test:
http://<addr>/auth/realms/<realm name>/protocol/openid-connect/registrations?client_id=<client id>&response_type=code&scope=openid%20email&redirect_uri=<redirect_uri>


**About the API endpoints:** 

You'll get 2 extra endpoints that are useful to do the verification from a custom application.

  + GET /auth/realms/{realmName}/sms/verification-code?phoneNumber=+5534990001234 (To request a number verification. No auth required.)
  + POST /auth/realms/{realmName}/sms/verification-code?phoneNumber=+5534990001234&code=123456 (To verify the process. User must be authenticated.)

You'll get 2 extra endpoints that are useful to do the OTP from a custom application.
  + GET /auth/realms/{realmName}/sms/authentication-code?phoneNumber=+5534990001234 (To request a number verification. No auth required.)
  + POST /auth/realms/shuashua/protocol/openid-connect/token
    Content-Type: application/x-www-form-urlencoded
    grant_type=password&phone_number=$PHONE_NUMBER&code=$VERIFICATION_CODE&client_id=$CLIENT_ID&client_secret=CLIENT_SECRECT


And then use Verification Code authentication flow with the code to obtain an access code.


## Thanks
Some code written is based on existing ones in these two projects: [keycloak-sms-provider](https://github.com/mths0x5f/keycloak-sms-provider)
and [keycloak-phone-authenticator](https://github.com/FX-HAO/keycloak-phone-authenticator). Certainly I would have many problems
coding all those providers blindly. Thank you!
