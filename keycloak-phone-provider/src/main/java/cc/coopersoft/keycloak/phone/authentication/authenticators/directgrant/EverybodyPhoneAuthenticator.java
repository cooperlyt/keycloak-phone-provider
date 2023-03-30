package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import cc.coopersoft.keycloak.phone.Utils;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;


public class EverybodyPhoneAuthenticator extends BaseDirectGrantAuthenticator {

  private static final Logger logger = Logger.getLogger(EverybodyPhoneAuthenticator.class);

  public EverybodyPhoneAuthenticator(KeycloakSession session) {
    if (session.getContext().getRealm() == null) {
      throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
    }
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    getPhoneNumber(context)
        .ifPresentOrElse(phoneNumber -> getAuthenticationCode(context)
                .ifPresentOrElse(code -> authToUser(context, phoneNumber, code),
                    ()-> invalidCredentials(context)),
            () -> invalidCredentials(context));
  }

  private void authToUser(AuthenticationFlowContext context, String phoneNumber, String code) {
    PhoneVerificationCodeProvider phoneVerificationCodeProvider = context.getSession().getProvider(PhoneVerificationCodeProvider.class);
    TokenCodeRepresentation tokenCode = phoneVerificationCodeProvider.ongoingProcess(phoneNumber, TokenCodeType.AUTH);

    if (tokenCode == null || !tokenCode.getCode().equals(code)) {
      invalidCredentials(context);
      return;
    }

    UserModel user = Utils.findUserByPhone(context.getSession(), context.getRealm(), phoneNumber)
        .orElseGet(() -> {
          if (context.getSession().users().getUserByUsername(context.getRealm(),phoneNumber) != null) {
            invalidCredentials(context, AuthenticationFlowError.USER_CONFLICT);
            return null;
          }
          UserModel newUser = context.getSession().users().addUser(context.getRealm(), phoneNumber);

          newUser.setEnabled(true);
          context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, phoneNumber);
          return newUser;
        });
    if (user != null) {
      context.setUser(user);
      phoneVerificationCodeProvider.tokenValidated(user, phoneNumber, tokenCode.getId(),false);
      context.success();
    }
  }
}
