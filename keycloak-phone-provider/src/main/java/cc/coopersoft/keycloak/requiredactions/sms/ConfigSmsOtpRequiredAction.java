package cc.coopersoft.keycloak.requiredactions.sms;

import cc.coopersoft.keycloak.authenticators.sms.SmsOtpCredentialProvider;
import cc.coopersoft.keycloak.authenticators.sms.SmsOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.authenticators.sms.credential.SmsOtpCredentialModel;
import cc.coopersoft.keycloak.providers.sms.spi.TokenCodeService;
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
            SmsOtpCredentialProvider socp = (SmsOtpCredentialProvider) context.getSession()
                    .getProvider(CredentialProvider.class, SmsOtpCredentialProviderFactory.PROVIDER_ID);
            socp.createCredential(context.getRealm(), context.getUser(), SmsOtpCredentialModel.create(phoneNumber));
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
