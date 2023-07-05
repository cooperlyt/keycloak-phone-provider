package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderService;
import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderServiceProviderFactory;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class BulksmsMessageSenderServiceProviderFactory implements MessageSenderServiceProviderFactory {

    private Scope config;

    @Override
    public MessageSenderService create(KeycloakSession session) {
        return new BulksmsSmsSenderServiceProvider(config,session.getContext().getRealm().getDisplayName());
    }

    @Override
    public void init(Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "bulksms";
    }
}
