package cc.coopersoft.keycloak.phone.authentication.requiredactions;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;

public class UpdatePhoneNumberRequiredAction implements RequiredActionProvider {

    public static final String PROVIDER_ID = "UPDATE_PHONE_NUMBER";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form()
                .createForm("login-update-phone-number.ftl");
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        PhoneVerificationCodeProvider phoneVerificationCodeProvider = context.getSession().getProvider(PhoneVerificationCodeProvider.class);
        String phoneNumber = context.getHttpRequest().getDecodedFormParameters().getFirst(SupportPhonePages.FIELD_PHONE_NUMBER);
        String code = context.getHttpRequest().getDecodedFormParameters().getFirst(SupportPhonePages.FIELD_VERIFICATION_CODE);
        try {
            phoneNumber = Utils.canonicalizePhoneNumber(context.getSession(),phoneNumber);
            phoneVerificationCodeProvider.validateCode(context.getUser(), phoneNumber, code);
            context.success();
        } catch (BadRequestException e) {

            Response challenge = context.form()
                    .setError(SupportPhonePages.Errors.NO_PROCESS.message())
                    .createForm("login-update-phone-number.ftl");
            context.challenge(challenge);

        } catch (ForbiddenException e) {

            Response challenge = context.form()
                    .setAttribute("phoneNumber", phoneNumber)
                    .setError(SupportPhonePages.Errors.NOT_MATCH.message())
                    .createForm("login-update-phone-number.ftl");
            context.challenge(challenge);
        } catch (PhoneNumberInvalidException e) {
            Response challenge = context.form()
                .setAttribute("phoneNumber", phoneNumber)
                .setError(e.getErrorType().message())
                .createForm("login-update-phone-number.ftl");
            context.challenge(challenge);
        }
    }

    @Override
    public void close() {
    }
}
