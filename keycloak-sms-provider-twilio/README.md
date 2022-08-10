# Twilio  SMS Sender Provider


```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-twilio.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-message-service-default-service=twilio \
  --spi-message-sender-service-twilio-account=${account} \
  --spi-message-sender-service-twilio-token=${token} \
  --spi-message-sender-service-twilio-number=${servicePhoneNumber} 
```
