# tencent  SMS Sender Provider

**Not verify in Quarkus 19.0.1**

```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-tencent.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-default-service=tencent \
  --spi-message-sender-service-tencent-secret=${secretId} \
  --spi-message-sender-service-tencent-key=${secretkey} \
  --spi-message-sender-service-tencent-region=${region} \
  --spi-message-sender-service-tencent-app=${AppId} \
  
  --spi-message-sender-service-tencent-opt-template={templateId} 
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
