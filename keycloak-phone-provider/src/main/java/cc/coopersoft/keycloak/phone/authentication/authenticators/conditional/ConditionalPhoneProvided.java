package cc.coopersoft.keycloak.phone.authentication.authenticators.conditional;

import cc.coopersoft.common.OptionalUtils;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalUserAttributeValueFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ConditionalPhoneProvided implements  ConditionalAuthenticator {

  static final ConditionalPhoneProvided SINGLETON = new ConditionalPhoneProvided();

  @Override
  public boolean matchCondition(AuthenticationFlowContext context) {
    var config = context.getAuthenticatorConfig().getConfig();
    boolean negateOutput = Boolean.parseBoolean(config.getOrDefault(ConditionalUserAttributeValueFactory.CONF_NOT,"false"));

    boolean result = OptionalUtils.ofBlank(OptionalUtils.ofBlank(
            context.getHttpRequest().getDecodedFormParameters().getFirst("phone_number"))
        .orElse(context.getHttpRequest().getDecodedFormParameters().getFirst("phoneNumber"))).isPresent();
    return negateOutput != result;
  }

  @Override
  public void action(AuthenticationFlowContext authenticationFlowContext) {
    // Not used
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    // Not used
  }

  @Override
  public void close() {
    // Does nothing
  }
}
