package cc.coopersoft.keycloak.phone.authentication.authenticators.conditional;

import org.keycloak.Config;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

public class ConditionalPhoneProvidedFactory implements ConditionalAuthenticatorFactory {

  public static final String PROVIDER_ID = "conditional-phone-provided";
  public static final String CONF_NOT = "not";

  private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
  };

  @Override
  public ConditionalAuthenticator getSingleton() {
    return ConditionalPhoneProvided.SINGLETON;
  }

  @Override
  public String getDisplayType() {
    return "Condition - phone provided";
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Flow is executed only if the phone number provided";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    ProviderConfigProperty negateOutput = new ProviderConfigProperty();
    negateOutput.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    negateOutput.setName(CONF_NOT);
    negateOutput.setLabel("Negate output");
    negateOutput.setHelpText("Apply a not to the check result");

    return Collections.singletonList(negateOutput);
  }

  @Override
  public void init(Config.Scope scope) {

  }

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

  }

  @Override
  public void close() {

  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
