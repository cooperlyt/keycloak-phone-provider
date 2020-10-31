FROM jboss/keycloak:10.0.2

WORKDIR /tmp

RUN curl -fSL https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/3.14.2/okhttp-3.14.2.jar -o okhttp-3.14.2.jar \
    && curl -fSL https://repo1.maven.org/maven2/com/squareup/okio/okio/1.17.2/okio-1.17.2.jar -o okio-1.17.2.jar

RUN curl -fSL https://repo1.maven.org/maven2/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar -o java-sms-sdk-1.0.3.jar

#ADD https://repo1.maven.org/maven2/com/squareup/okio/okio/1.17.2/okio-1.17.2.jar .
#ADD https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/3.14.2/okhttp-3.14.2.jar .
COPY modules/keycloak-phone-provider/main/keycloak-phone-provider.jar .
COPY modules/keycloak-sms-provider-dummy/main/keycloak-sms-provider-dummy.jar .

#ADD https://repo1.maven.org/maven2/com/cloopen/java-sms-sdk/1.0.3/java-sms-sdk-1.0.3.jar .
COPY modules/keycloak-sms-provider-cloopen/main/keycloak-sms-provider-cloopen.jar .

COPY module-add.cli cli/
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=cli/module-add.cli
RUN rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current
COPY standalone/deployments/keycloak-phone-provider.resources.jar $JBOSS_HOME/standalone/deployments/



