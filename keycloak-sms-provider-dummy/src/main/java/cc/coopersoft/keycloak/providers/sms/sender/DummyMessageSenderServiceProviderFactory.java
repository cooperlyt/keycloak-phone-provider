package cc.coopersoft.keycloak.providers.sms.sender;

import cc.coopersoft.keycloak.providers.sms.spi.MessageSenderService;
import cc.coopersoft.keycloak.providers.sms.spi.MessageSenderServiceProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DummyMessageSenderServiceProviderFactory implements MessageSenderServiceProviderFactory {

    @Override
    public MessageSenderService create(KeycloakSession keycloakSession) {
        return new DummyMessageSenderService();
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "Dummy";
    }
}
