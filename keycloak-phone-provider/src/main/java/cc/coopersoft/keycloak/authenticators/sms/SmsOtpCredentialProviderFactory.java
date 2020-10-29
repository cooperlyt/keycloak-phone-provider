package cc.coopersoft.keycloak.authenticators.sms;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

public class SmsOtpCredentialProviderFactory implements CredentialProviderFactory<SmsOtpCredentialProvider> {

    public final static String PROVIDER_ID = "sms-otp";

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new SmsOtpCredentialProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
