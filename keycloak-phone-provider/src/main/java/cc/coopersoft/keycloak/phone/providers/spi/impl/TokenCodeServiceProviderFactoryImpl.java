package cc.coopersoft.keycloak.phone.providers.spi.impl;

import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeServiceProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class TokenCodeServiceProviderFactoryImpl implements TokenCodeServiceProviderFactory {

    private Config.Scope config;

    @Override
    public TokenCodeService create(KeycloakSession session) {
        return new TokenCodeServiceImpl(session, config);
    }

    @Override
    public void init(Config.Scope config) {
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
        return "TokenCodeServiceProviderFactoryImpl";
    }
}
