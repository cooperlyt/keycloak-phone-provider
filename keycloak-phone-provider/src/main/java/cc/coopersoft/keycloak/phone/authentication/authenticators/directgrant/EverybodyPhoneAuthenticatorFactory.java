package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class EverybodyPhoneAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {


  public static final String PROVIDER_ID = "everybody-phone-authenticator";

  @Override
  public Authenticator create(KeycloakSession session){
    return new EverybodyPhoneAuthenticator(session);
  }

  @Override
  public void init(Config.Scope config) {

  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {

  }

  @Override
  public void close() {

  }


  @Override
  public String getDisplayType() {
    return "Authentication everybody by phone";
  }

  @Override
  public String getHelpText() {
    return "Authentication everybody by phone";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
  }

  @Override
  public String getReferenceCategory() {
    return "Everybody phone Grant";
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED,
      AuthenticationExecutionModel.Requirement.DISABLED
  };

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return true;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
