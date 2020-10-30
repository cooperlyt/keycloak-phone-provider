package cc.coopersoft.keycloak.phone.authentication.requiredactions;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UpdatePhoneNumberRequiredActionFactory implements RequiredActionFactory {

    private static final UpdatePhoneNumberRequiredAction instance = new UpdatePhoneNumberRequiredAction();

    @Override
    public String getDisplayText() {
        return "Update Phone Number";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return instance;
    }

    @Override
    public void init(Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory sessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return UpdatePhoneNumberRequiredAction.PROVIDER_ID;
    }
}
