# Twilio  SMS Sender Provider

**Not verify in Quarkus 19.0.1**

```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-twilio.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-default-service=twilio \
  --spi-message-sender-service-twilio-account=${account} \
  --spi-message-sender-service-twilio-token=${token} \
  --spi-message-sender-service-twilio-number=${servicePhoneNumber} 
```
