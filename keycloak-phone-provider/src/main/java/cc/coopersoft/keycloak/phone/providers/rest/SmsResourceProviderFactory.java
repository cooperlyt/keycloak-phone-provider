package cc.coopersoft.keycloak.phone.providers.rest;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;


public class SmsResourceProviderFactory implements RealmResourceProviderFactory {

    private static final Logger logger = Logger.getLogger(SmsResourceProviderFactory.class);

    @Override
    public String getId() {
        return "sms";
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new SmsResourceProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}