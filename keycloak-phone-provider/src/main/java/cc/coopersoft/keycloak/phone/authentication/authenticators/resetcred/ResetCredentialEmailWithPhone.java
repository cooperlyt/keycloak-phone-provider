package cc.coopersoft.keycloak.phone.authentication.authenticators.resetcred;

import org.apache.commons.lang.StringUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.resetcred.ResetCredentialEmail;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;

public class ResetCredentialEmailWithPhone extends ResetCredentialEmail {
    public static final Requirement[] REQUIREMENT_CHOICES;

    static {
        REQUIREMENT_CHOICES = new Requirement[]{
            Requirement.CONDITIONAL,
            Requirement.DISABLED
        };
    }
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (context.getExecution().isRequired() ||
                (context.getExecution().isConditional() &&
                        configuredFor(context))) {
            super.authenticate(context);
        } else {
            context.success();
        }
    }

    protected boolean configuredFor(AuthenticationFlowContext context) {
        String sendNote = context.getAuthenticationSession().getAuthNote(ResetCredentialWithPhone.NOT_SEND_EMAIL);
        return !"false".equalsIgnoreCase(sendNote);
    }

    @Override
    public String getId() {
        return "reset-credential-email-with-phone";
    }

    @Override
    public String getDisplayType() {
        return "Send Reset Email If Not Phone";
    }

    @Override
    public String getHelpText() {
        return "Send email to user if not phone provided.";
    }



    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

}
