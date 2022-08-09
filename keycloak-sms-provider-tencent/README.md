# tencent  SMS Sender Provider


```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-tencent.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-message-service-default-service=tencent \
  --spi-message-sender-service-tencent-secret=${secretId} \
  --spi-message-sender-service-tencent-key=${secretkey} \
  --spi-message-sender-service-tencent-region=${region} \
  --spi-message-sender-service-tencent-app=${AppId} \
  
  --spi-message-sender-service-tencent-opt-template={templateId} 
```

```
templateId is: [realm-]<type>-<template>


type: 
    VERIFY("verification"),
    OTP("authentication"),
    RESET("reset credential"),
    REGISTRATION("registration");
```
