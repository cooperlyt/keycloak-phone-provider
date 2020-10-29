package cc.coopersoft.keycloak.providers.sms.spi.impl;

import cc.coopersoft.keycloak.providers.sms.spi.TokenCodeService;
import cc.coopersoft.keycloak.providers.sms.spi.TokenCodeServiceProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class TokenCodeServiceProviderFactoryImpl implements TokenCodeServiceProviderFactory {

    @Override
    public TokenCodeService create(KeycloakSession session) {
        return new TokenCodeServiceImpl(session);
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
        return "TokenCodeServiceProviderFactoryImpl";
    }
}
