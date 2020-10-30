package cc.coopersoft.keycloak.phone.authentication.requiredactions;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ConfigSmsOtpRequiredActionFactory implements RequiredActionFactory {

    private static final ConfigSmsOtpRequiredAction instance = new ConfigSmsOtpRequiredAction();

    @Override
    public String getDisplayText() {
        return "Configure OTP over SMS";
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
        return ConfigSmsOtpRequiredAction.PROVIDER_ID;
    }
}
