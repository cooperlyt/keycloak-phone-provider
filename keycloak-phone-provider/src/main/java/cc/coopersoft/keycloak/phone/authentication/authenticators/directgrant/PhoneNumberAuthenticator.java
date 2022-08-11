package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.keycloak.phone.utils.UserUtils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.Response;

public class PhoneNumberAuthenticator extends BaseDirectGrantAuthenticator {

    private static final Logger logger = Logger.getLogger(PhoneNumberAuthenticator.class);

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        //TODO is`s is invalid? remove it.
        user.addRequiredAction("PHONE_NUMBER_GRANT_CONFIG");
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.clearUser();
        getPhoneNumber(context).ifPresentOrElse(phoneNumber ->
            UserUtils.findUserByPhone(context.getSession().users(),context.getRealm(),phoneNumber)
                .ifPresentOrElse(user -> {
                    context.setUser(user);
                    context.success();
                },()->invalidCredentials(context)),() -> invalidCredentials(context));
    }
}
