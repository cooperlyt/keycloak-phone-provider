# YUNXin  SMS Sender Provider


```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-yunxin.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-message-service-default-service=yunxin \
  --spi-message-sender-service-yunxin-secret=${secret} \
  --spi-message-sender-service-yunxin-app=${AppId} \
  --spi-message-sender-service-yunxin-opt-template=${templateId} 
```
```
templateId is: [realm-]<type>-<template>

type: 
    VERIFY("verification"),
    OTP("authentication"),
    RESET("reset credential"),
    REGISTRATION("registration");
```