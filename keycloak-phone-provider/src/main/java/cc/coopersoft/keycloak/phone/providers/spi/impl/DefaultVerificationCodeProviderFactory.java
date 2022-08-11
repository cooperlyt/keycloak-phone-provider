package cc.coopersoft.keycloak.phone.providers.spi.impl;

import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultVerificationCodeProviderFactory implements PhoneVerificationCodeProviderFactory {

    @Override
    public PhoneVerificationCodeProvider create(KeycloakSession session) {
        return new DefaultPhoneVerificationCodeProvider(session);
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
        return "default";
    }
}
