package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;
import java.util.Optional;


public class AuthenticationCodeAuthenticator extends BaseDirectGrantAuthenticator {

    private static final Logger logger = Logger.getLogger(AuthenticationCodeAuthenticator.class);

    public AuthenticationCodeAuthenticator(KeycloakSession session) {
        if (session.getContext().getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction("VERIFICATION_CODE_GRANT_CONFIG");
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (!validateVerificationCode(context,getPhoneNumber(context))) {
            invalidCredentials(context,context.getUser());
            return;
        }
        context.success();
    }



    private boolean validateVerificationCode(AuthenticationFlowContext context, String phoneNumber) {

        String code = getAuthenticationCode(context);

//        String kind = null;
//        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
//        if (authenticatorConfig != null && authenticatorConfig.getConfig() != null) {
//            kind = Optional.ofNullable(context.getAuthenticatorConfig().getConfig().get(AuthenticationCodeAuthenticatorFactory.KIND)).orElse("");
//        }

        try {
            context.getSession().getProvider(TokenCodeService.class).validateCode(context.getUser(), phoneNumber, code, TokenCodeType.OTP);
            return true;
        } catch (Exception e) {
            logger.info("Grant authenticator valid code failure",e);
            return false;
        }

    }

}
