# cloopen  SMS Sender Provider

https://www.yuntongxun.com

**Not verify in Quarkus 19.0.1**

```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-cloopen.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-default-service=cloopen \
  --spi-message-sender-service-cloopen-app=${appId} \
  --spi-message-sender-service-cloopen-account=${account} \
  --spi-message-sender-service-cloopen-token=${token} \
  --spi-message-sender-service-cloopen-opt-template=${templateId} 
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
