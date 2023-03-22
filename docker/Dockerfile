
FROM quay.io/keycloak/keycloak:@version.keycloak@


  # yuntongxin sdk
  # totalvoice sdk
  # twilio sdk
  # tencent sdk
  # aliyun sdk
#RUN curl -fSL https://repo1.maven.org/maven2/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar -o java-sms-sdk-1.0.3.jar \
#    && curl -fSL https://repo1.maven.org/maven2/br/com/totalvoice/totalvoice-java/1.1.1/totalvoice-java-1.1.1.jar -o totalvoice-java-1.1.1.jar \
#    && curl -fSL https://repo1.maven.org/maven2/com/twilio/sdk/twilio/7.53.0/twilio-7.53.0.jar -o twilio-7.53.0.jar \
#    && curl -fSL https://repo1.maven.org/maven2/com/tencentcloudapi/tencentcloud-sdk-java/3.1.158/tencentcloud-sdk-java-3.1.158.jar -o tencentcloud-sdk-java-3.1.158.jar \
#    && curl -fSL https://repo1.maven.org/maven2/com/aliyun/aliyun-java-sdk-core/4.5.3/aliyun-java-sdk-core-4.5.3.jar -o aliyun-java-sdk-core-4.5.3.jar \
#    && curl -fSL https://repo1.maven.org/maven2/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar -o ini4j-0.5.4.jar \
#    && curl -fSL https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.5/org.jacoco.agent-0.8.5-runtime.jar -o org.jacoco.agent-0.8.5-runtime.jar
# yuntongxin provider need gson 2.8.6, keycloak 10.0.2 gson version is low need higher
#RUN curl -fSL https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar -o gson-2.8.6.jar

# main provider
COPY providers/keycloak-phone-provider.jar /opt/keycloak/providers/

#resources
COPY providers/keycloak-phone-provider.resources.jar /opt/keycloak/providers/

#dummy provider
COPY providers/keycloak-sms-provider-dummy.jar /opt/keycloak/providers/

#yuntongxin provider
COPY providers/keycloak-sms-provider-cloopen.jar /opt/keycloak/providers/

#totalvoice provider
COPY providers/keycloak-sms-provider-totalvoice.jar /opt/keycloak/providers/

#twilio provider
COPY providers/keycloak-sms-provider-twilio.jar /opt/keycloak/providers/

#yunxin provider
COPY providers/keycloak-sms-provider-yunxin.jar /opt/keycloak/providers/

#tencent provider
COPY providers/keycloak-sms-provider-tencent.jar /opt/keycloak/providers/
#
#aliyun provider
COPY providers/keycloak-sms-provider-aliyun.jar /opt/keycloak/providers/

#aws provider
COPY providers/keycloak-sms-provider-aws-sns.jar /opt/keycloak/providers/

RUN mkdir /opt/keycloak/certs

RUN /opt/keycloak/bin/kc.sh build




