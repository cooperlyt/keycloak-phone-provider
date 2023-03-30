package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.keycloak.phone.authentication.authenticators.resetcred.ResetCredentialWithPhone;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class SmsOtpMfaAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public final static String PROVIDER_ID = "sms-otp-authenticator";

//    public final static String COOKIE_MAX_AGE= "cookieMaxAge";
    private final static SmsOtpMfaAuthenticator instance = new SmsOtpMfaAuthenticator();

    @Override
    public String getDisplayType() {
        return "OTP over SMS";
    }

    @Override
    public String getReferenceCategory() {
        return "OTP over SMS";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Asks for a token sent to users' mobile phone";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
//        ProviderConfigProperty rep =
//            new ProviderConfigProperty(COOKIE_MAX_AGE,
//                "Cookie Max Age",
//                "Max age in seconds of the SMS_OTP_COOKIE. Zero is Don't use Cookie",
//                STRING_TYPE, "3600");
        return null;
    }

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return instance;
    }

    @Override
    public void init(Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
