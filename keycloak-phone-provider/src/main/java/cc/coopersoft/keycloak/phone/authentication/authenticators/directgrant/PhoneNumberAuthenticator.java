package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.keycloak.phone.Utils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class PhoneNumberAuthenticator extends BaseDirectGrantAuthenticator {

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.clearUser();
        getPhoneNumber(context).ifPresentOrElse(
                phoneNumber -> Utils.findUserByPhone(context.getSession(), context.getRealm(), phoneNumber)
                        .ifPresentOrElse(user -> {
                            context.setUser(user);
                            context.success();
                        }, () -> invalidCredentials(context)),
                () -> invalidCredentials(context));
    }
}
