# Whatsapp SMS Sender Provider

**Not verify **

```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-whatsapp.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-default-service=whatsapp \
  --spi-message-sender-service-whatsapp-access-token=${access_token} \
  --spi-message-sender-service-whatsapp-phone-number-id=${phone_number_id} \
  --spi-message-sender-service-whatsapp-template-name=${template_name} \
  --spi-message-sender-service-whatsapp-template-language=${template_language} 
```
