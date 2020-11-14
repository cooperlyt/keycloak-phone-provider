FROM coopersoft/keycloak-callback:11.0.3

WORKDIR /tmp

  # yuntongxin sdk
  # totalvoice sdk
  # twilio sdk
  # tencent sdk
  # aliyun sdk
RUN curl -fSL https://repo1.maven.org/maven2/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar -o java-sms-sdk-1.0.3.jar \
    && curl -fSL https://repo1.maven.org/maven2/br/com/totalvoice/totalvoice-java/1.1.1/totalvoice-java-1.1.1.jar -o totalvoice-java-1.1.1.jar \
    && curl -fSL https://repo1.maven.org/maven2/com/twilio/sdk/twilio/7.53.0/twilio-7.53.0.jar -o twilio-7.53.0.jar \
    && curl -fSL https://repo1.maven.org/maven2/com/tencentcloudapi/tencentcloud-sdk-java/3.1.158/tencentcloud-sdk-java-3.1.158.jar -o tencentcloud-sdk-java-3.1.158.jar \
    && curl -fSL https://repo1.maven.org/maven2/com/aliyun/aliyun-java-sdk-core/4.5.3/aliyun-java-sdk-core-4.5.3.jar -o aliyun-java-sdk-core-4.5.3.jar \
    && curl -fSL https://repo1.maven.org/maven2/org/ini4j/ini4j/0.5.4/ini4j-0.5.4.jar -o ini4j-0.5.4.jar \
    && curl -fSL https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.5/org.jacoco.agent-0.8.5-runtime.jar -o org.jacoco.agent-0.8.5-runtime.jar
# yuntongxin provider need gson 2.8.6, keycloak 10.0.2 gson version is low need higher
#RUN curl -fSL https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar -o gson-2.8.6.jar

# main provider
COPY modules/keycloak-phone-provider/main/keycloak-phone-provider.jar .

#dummy provider
COPY modules/keycloak-sms-provider-dummy/main/keycloak-sms-provider-dummy.jar .

#yuntongxin provider
COPY modules/keycloak-sms-provider-cloopen/main/keycloak-sms-provider-cloopen.jar .

#totalvoice provider
COPY modules/keycloak-sms-provider-totalvoice/main/keycloak-sms-provider-totalvoice.jar .

#twilio provider
COPY modules/keycloak-sms-provider-twilio/main/keycloak-sms-provider-twilio.jar .

#yunxin provider
COPY modules/keycloak-sms-provider-yunxin/main/keycloak-sms-provider-yunxin.jar .

#tencent provider
COPY modules/keycloak-sms-provider-tencent/main/keycloak-sms-provider-tencent.jar .

#aliyun provider
COPY modules/keycloak-sms-provider-aliyun/main/keycloak-sms-provider-aliyun.jar .

COPY module-add.cli cli/
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=cli/module-add.cli
RUN rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current

COPY standalone/deployments/keycloak-phone-provider.resources.jar $JBOSS_HOME/standalone/deployments/



