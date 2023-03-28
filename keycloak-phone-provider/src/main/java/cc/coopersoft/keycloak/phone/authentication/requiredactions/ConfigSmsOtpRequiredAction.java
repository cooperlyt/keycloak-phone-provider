package cc.coopersoft.keycloak.phone.authentication.requiredactions;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialProvider;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;

public class ConfigSmsOtpRequiredAction implements RequiredActionProvider {

    public static final String PROVIDER_ID = "CONFIGURE_SMS_OTP";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {

        Response challenge = context.form()
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
            phoneVerificationCodeProvider.validateCode(context.getUser(), phoneNumber, code);
            PhoneOtpCredentialProvider socp = (PhoneOtpCredentialProvider) context.getSession()
                    .getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);
            socp.createCredential(context.getRealm(), context.getUser(), PhoneOtpCredentialModel.create(phoneNumber));
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
                    .createForm("login-update-phone-number.ftl");
            context.challenge(challenge);
        } catch (PhoneNumberInvalidException e) {
            Response challenge = context.form()
                .setError(e.getErrorType().message())
                .createForm("login-update-phone-number.ftl");
            context.challenge(challenge);
        }
    }

    @Override
    public void close() {
    }
}
