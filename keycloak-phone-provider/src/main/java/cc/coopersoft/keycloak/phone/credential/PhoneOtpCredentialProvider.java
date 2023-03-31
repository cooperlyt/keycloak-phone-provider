package cc.coopersoft.keycloak.phone.credential;

import cc.coopersoft.keycloak.phone.authentication.authenticators.browser.SmsOtpMfaAuthenticatorFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import org.jboss.logging.Logger;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;
import org.keycloak.credential.*;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.dto.OTPSecretData;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Optional;

/**
 *  证书使用 CredentialValidator 来认证，例如 password 证书 使用登录认证，本例中使用 phone OTP 认证
 *   //not have credential , SmsOtpMfaAuthenticator setRequiredActions will add
 *   ConfigSmsOtpRequiredAction to add an OPT credential
 *   -> OTP
 */
public class PhoneOtpCredentialProvider implements CredentialProvider<PhoneOtpCredentialModel>, CredentialInputValidator {

    private final static Logger logger = Logger.getLogger(PhoneOtpCredentialProvider.class);
    private final KeycloakSession session;

    public PhoneOtpCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    private PhoneVerificationCodeProvider getTokenCodeService() {
        return session.getProvider(PhoneVerificationCodeProvider.class);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        return user.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny().isPresent();
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        logger.info("---------------begin valid otp sms");

        String phoneNumber = user.getFirstAttribute("phoneNumber");


        String code = input.getChallengeResponse();

        if (!(input instanceof UserCredentialModel)) return false;
        if (!input.getType().equals(getType())) return false;
        if (phoneNumber == null) return false;
        if (code == null) return false;


        if (ObjectUtil.isBlank(input.getCredentialId())) {
            logger.debugf("CredentialId is null when validating credential of user %s", user.getUsername());
            return false;
        }

        CredentialModel credential = user.credentialManager().getStoredCredentialById(input.getCredentialId());
        var invalid = Optional.ofNullable(user.credentialManager().getStoredCredentialById(input.getCredentialId()))
            .map(credentialModel -> {
                try {
                    return JsonSerialization.readValue(credentialModel.getCredentialData(), PhoneOtpCredentialModel.SmsOtpCredentialData.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .map(PhoneOtpCredentialModel.SmsOtpCredentialData::isSecretInvalid)
            .filter(invalidSecret -> invalidSecret)
            .orElse(false);
        if (invalid){
            try {
                getTokenCodeService().validateCode(user, phoneNumber, code, TokenCodeType.OTP);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return Optional.ofNullable(credential.getSecretData())
            .map(secretData -> {
                try {
                    return JsonSerialization.readValue(secretData, OTPSecretData.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            })
            .flatMap(secretData -> Optional.ofNullable(secretData.getValue()))
            .map(CredentialCode -> CredentialCode.equals(code))
            .orElse(false);
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
        return user.credentialManager().createStoredCredential(credential);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
//        return getCredentialStore().removeStoredCredential(realm, user, credentialId);
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
