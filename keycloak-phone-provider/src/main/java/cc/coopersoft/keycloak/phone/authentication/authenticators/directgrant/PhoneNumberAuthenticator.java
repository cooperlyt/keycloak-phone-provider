package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.keycloak.phone.utils.UserUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;

public class PhoneNumberAuthenticator extends BaseDirectGrantAuthenticator {

    private static final Logger logger = Logger.getLogger(PhoneNumberAuthenticator.class);

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction("PHONE_NUMBER_GRANT_CONFIG");
    }

    protected UserModel findUser(AuthenticationFlowContext context) {
        return UserUtils.findUserByPhone(context.getSession().users(),context.getRealm(),context.getHttpRequest().getDecodedFormParameters().getFirst("phone_number"));
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = findUser(context);
        if (user == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challenge = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challenge);
            logger.info("Grant authenticator valid phone failure");
            return;
        }

        logger.info("Grant authenticator valid phone success");
        context.setUser(user);
        context.success();
    }
}
