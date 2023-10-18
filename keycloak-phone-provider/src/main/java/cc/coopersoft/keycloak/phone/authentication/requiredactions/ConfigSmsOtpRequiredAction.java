package cc.coopersoft.keycloak.phone.authentication.requiredactions;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialProvider;

import java.util.Optional;

import static cc.coopersoft.keycloak.phone.authentication.authenticators.browser.PhoneUsernamePasswordForm.VERIFIED_PHONE_NUMBER;

public class ConfigSmsOtpRequiredAction implements RequiredActionProvider {

    public static final String PROVIDER_ID = "CONFIGURE_SMS_OTP";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

//    private Optional<PhoneOtpCredentialModel> getOTPCredential(RequiredActionContext context){
//        return Optional.ofNullable(context.getUser())
//                .flatMap(user -> user.credentialManager().getStoredCredentialsByTypeStream(PhoneOtpCredentialModel.TYPE).findFirst())
//                .map(credentialModel -> (PhoneOtpCredentialModel) credentialModel);
//    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {

        var userPhoneNumber = PhoneOtpCredentialModel.getSmsOtpCredentialData(context.getUser())
            .map(PhoneOtpCredentialModel.SmsOtpCredentialData::getPhoneNumber)
            .orElseGet(() -> Optional.ofNullable(context.getUser())
                .flatMap(user -> Optional.ofNullable(user.getFirstAttribute(SupportPhonePages.FIELD_PHONE_NUMBER)))
                .orElse(null)
            );

        Response challenge = context.form()
            .setAttribute(SupportPhonePages.FIELD_PHONE_NUMBER, userPhoneNumber)
                .createForm("login-sms-otp-config.ftl");
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        var session = context.getSession();
        PhoneVerificationCodeProvider phoneVerificationCodeProvider = session.getProvider(PhoneVerificationCodeProvider.class);
        String phoneNumber = context.getHttpRequest().getDecodedFormParameters().getFirst(SupportPhonePages.FIELD_PHONE_NUMBER);
        String code = context.getHttpRequest().getDecodedFormParameters().getFirst(SupportPhonePages.FIELD_VERIFICATION_CODE);
        try {
            phoneNumber = Utils.canonicalizePhoneNumber(context.getSession(),phoneNumber);
            phoneVerificationCodeProvider.validateCode(context.getUser(), phoneNumber, code, TokenCodeType.OTP);

            PhoneOtpCredentialProvider ocp = (PhoneOtpCredentialProvider) context.getSession()
                    .getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);
            ocp.createCredential(context.getRealm(), context.getUser(),
                PhoneOtpCredentialModel.create(phoneNumber,code,Utils.getOtpExpires(context.getSession())));
            context.getAuthenticationSession().setAuthNote(VERIFIED_PHONE_NUMBER, phoneNumber);
            context.success();
        } catch (BadRequestException e) {

            Response challenge = context.form()
                    .setError(SupportPhonePages.Errors.NO_PROCESS.message())
                    .createForm("login-sms-otp-config.ftl");
            context.challenge(challenge);

        } catch (ForbiddenException e) {

            Response challenge = context.form()
                    .setAttribute("phoneNumber", phoneNumber)
                    .setError(SupportPhonePages.Errors.NOT_MATCH.message())
                    .createForm("login-sms-otp-config.ftl");
            context.challenge(challenge);
        } catch (PhoneNumberInvalidException e) {
            Response challenge = context.form()
                .setError(e.getErrorType().message())
                .createForm("login-sms-otp-config.ftl");
            context.challenge(challenge);
        }
    }

    @Override
    public void close() {
    }
}
