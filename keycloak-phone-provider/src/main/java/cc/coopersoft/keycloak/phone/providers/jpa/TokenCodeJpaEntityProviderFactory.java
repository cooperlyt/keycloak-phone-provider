package cc.coopersoft.keycloak.phone.providers.jpa;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class TokenCodeJpaEntityProviderFactory implements JpaEntityProviderFactory {

    @Override
    public JpaEntityProvider create(KeycloakSession session) {
        return new TokenCodeJpaEntityProvider();
    }

    @Override
    public String getId() {
        return "tokenCodeEntityProvider";
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
