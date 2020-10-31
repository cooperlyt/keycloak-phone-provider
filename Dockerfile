FROM jboss/keycloak:10.0.2

WORKDIR /tmp

RUN curl -fSL https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/3.14.2/okhttp-3.14.2.jar -o okhttp-3.14.2.jar \
    && curl -fSL https://repo1.maven.org/maven2/com/squareup/okio/okio/1.17.2/okio-1.17.2.jar -o okio-1.17.2.jar

RUN curl -fSL https://repo1.maven.org/maven2/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar -o java-sms-sdk-1.0.3.jar


#COPY build/okhttp-3.14.2.jar .
#COPY build/okio-1.17.2.jar .
COPY build/modules/keycloak-phone-provider/main/keycloak-phone-provider.jar .
COPY build/modules/keycloak-sms-provider-dummy/main/keycloak-sms-provider-dummy.jar .

#COPY build/java-sms-sdk-1.0.3.jar .
COPY build/modules/keycloak-sms-provider-cloopen/main/keycloak-sms-provider-cloopen.jar .

COPY jboss-cli/ cli/

RUN $JBOSS_HOME/bin/jboss-cli.sh --file=cli/module-add.cli

COPY build/standalone/deployments/keycloak-phone-provider.resources.jar $JBOSS_HOME/standalone/deployments/


