# Aliyun  SMS Sender Provider

www.aliyun.com

**verify on Quarkus 21.0.1 #35 **

```sh
cp target/providers/keycloak-phone-provider.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-phone-provider.resources.jar ${KEYCLOAK_HOME}/providers/
cp target/providers/keycloak-sms-provider-aliyun.jar ${KEYCLOAK_HOME}/providers/


${KEYCLOAK_HOME}/bin/kc.sh build

${KEYCLOAK_HOME}/bin/kc.sh start  --spi-phone-default-service=aliyun \
  --spi-message-sender-service-aliyun-region=cn-hangzhou \
  --spi-message-sender-service-aliyun-key=${accessKey} \
  --spi-message-sender-service-aliyun-secret=${accessSecret} \
  --spi-message-sender-service-aliyun-otp-template={templateId} 
```

```

SignName is realm id.

templateId is: [$realm-]<[$type | $kind]>-<template>

type: 
    VERIFY("verification"),
    AUTH("authentication"),
    OTP("OTP"),
    RESET("reset credential"),
    REGISTRATION("registration");
```
