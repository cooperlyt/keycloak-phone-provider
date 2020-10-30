package cc.coopersoft.keycloak.phone.authentication.requiredactions;

import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
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
        TokenCodeService tokenCodeService = context.getSession().getProvider(TokenCodeService.class);
        String phoneNumber = context.getHttpRequest().getDecodedFormParameters().getFirst("phoneNumber");
        String code = context.getHttpRequest().getDecodedFormParameters().getFirst("code");
        try {
            tokenCodeService.validateCode(context.getUser(), phoneNumber, code);
            PhoneOtpCredentialProvider socp = (PhoneOtpCredentialProvider) context.getSession()
                    .getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);
            socp.createCredential(context.getRealm(), context.getUser(), PhoneOtpCredentialModel.create(phoneNumber));
            context.success();
        } catch (BadRequestException e) {

            Response challenge = context.form()
                    .setError("noOngoingVerificationProcess")
                    .createForm("login-sms-otp-config.ftl");
            context.challenge(challenge);

        } catch (ForbiddenException e) {

            Response challenge = context.form()
                    .setAttribute("phoneNumber", phoneNumber)
                    .setError("verificationCodeDoesNotMatch")
                    .createForm("login-update-phone-number.ftl");
            context.challenge(challenge);
        }
    }

    @Override
    public void close() {
    }
}
