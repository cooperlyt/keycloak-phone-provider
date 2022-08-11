package cc.coopersoft.keycloak.phone.providers.spi.impl;

import cc.coopersoft.keycloak.phone.providers.spi.PhoneSupportProvider;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneSupportProviderFactory;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultPhoneSupportProviderFactory implements PhoneSupportProviderFactory {

    private Scope config;

    @Override
    public PhoneSupportProvider create(KeycloakSession session) {
        return new DefaultPhoneSupportProvider(session, config);
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
        return "default";
    }
}
