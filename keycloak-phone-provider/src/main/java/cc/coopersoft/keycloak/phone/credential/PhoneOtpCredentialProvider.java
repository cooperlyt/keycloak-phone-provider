package cc.coopersoft.keycloak.phone.credential;

import cc.coopersoft.keycloak.phone.authentication.authenticators.browser.SmsOtpMfaAuthenticatorFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.credential.*;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

public class PhoneOtpCredentialProvider implements CredentialProvider<PhoneOtpCredentialModel>, CredentialInputValidator {

    private final static Logger logger = Logger.getLogger(PhoneOtpCredentialProvider.class);
    private final KeycloakSession session;

    public PhoneOtpCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    private UserCredentialStore getCredentialStore() {
        return session.userCredentialManager();
    }

    private TokenCodeService getTokenCodeService() {
        return session.getProvider(TokenCodeService.class);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        return !getCredentialStore().getStoredCredentialsByType(realm, user, credentialType).isEmpty();
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {

        String phoneNumber = user.getFirstAttribute("phoneNumber");
        String code = input.getChallengeResponse();

        if (!(input instanceof UserCredentialModel)) return false;
        if (!input.getType().equals(getType())) return false;
        if (phoneNumber == null) return false;
        if (code == null) return false;

        try {
            getTokenCodeService().validateCode(user, phoneNumber, code, TokenCodeType.OTP);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getType() {
        return PhoneOtpCredentialModel.TYPE;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, PhoneOtpCredentialModel credential) {
        if (credential.getCreatedDate() == null) {
            credential.setCreatedDate(Time.currentTimeMillis());
        }
        return getCredentialStore().createCredential(realm, user, credential);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return getCredentialStore().removeStoredCredential(realm, user, credentialId);
    }

    @Override
    public PhoneOtpCredentialModel getCredentialFromModel(CredentialModel credentialModel) {
        return PhoneOtpCredentialModel.createFromCredentialModel(credentialModel);
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext credentialTypeMetadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .helpText("")
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(PhoneOtpCredentialProviderFactory.PROVIDER_ID)
                .createAction(SmsOtpMfaAuthenticatorFactory.PROVIDER_ID)
                .removeable(true)
                .build(session);
    }
}
