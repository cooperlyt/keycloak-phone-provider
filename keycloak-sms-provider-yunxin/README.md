# YUNXin  SMS Sender Provider

**Not verify in Quarkus 19.0.1**

```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-yunxin.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-default-service=yunxin \
  --spi-message-sender-service-yunxin-secret=${secret} \
  --spi-message-sender-service-yunxin-app=${AppId} \
  --spi-message-sender-service-yunxin-opt-template=${templateId} 
```
```
templateId is: [$realm-]<[$type | $kind]>-<template>

type: 
    VERIFY("verification"),
    AUTH("authentication"),
    OTP("OTP"),
    RESET("reset credential"),
    REGISTRATION("registration");
```