# Aliyun  SMS Sender Provider

www.aliyun.com


```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-aliyun.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-message-service-default-service=aliyun \
  --spi-message-sender-service-aliyun-region=cn-hangzhou \
  --spi-message-sender-service-aliyun-key=${accessKey} \
  --spi-message-sender-service-aliyun-secret=${accessSecret} \
  --spi-message-sender-service-aliyun-opt-template={templateId} 
```

```
templateId is: [realm-]<type>-<template>
SignName is realm id.

type: 
    VERIFY("verification"),
    OTP("authentication"),
    RESET("reset credential"),
    REGISTRATION("registration");
```