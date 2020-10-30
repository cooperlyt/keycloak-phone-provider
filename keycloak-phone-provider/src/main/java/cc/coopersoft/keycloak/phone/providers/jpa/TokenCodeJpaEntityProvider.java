package cc.coopersoft.keycloak.phone.providers.jpa;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

import java.util.Collections;
import java.util.List;

public class TokenCodeJpaEntityProvider implements JpaEntityProvider {

    @Override
    public List<Class<?>> getEntities() {
        return Collections.singletonList(TokenCode.class);
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/changelog/token-code-changelog.xml";
    }

    @Override
    public void close() {
    }

    @Override
    public String getFactoryId() {
        return "tokenCodeEntityProvider";
    }
}