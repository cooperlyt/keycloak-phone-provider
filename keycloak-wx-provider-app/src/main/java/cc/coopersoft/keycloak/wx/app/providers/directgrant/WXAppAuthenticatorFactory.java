package cc.coopersoft.keycloak.wx.app.providers.directgrant;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Collections;
import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

public class WXAppAuthenticatorFactory implements AuthenticatorFactory {



  public static final String PROVIDER_ID = "wx-app-authenticator";

  public static final String CONFIG_EVERY = "every";

  public static final String WX_API_ID = "appid";

  public static final String WX_API_SECRET = "app_secret";

  private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED,
  };

  private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

  static {
    CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property().name(CONFIG_EVERY)
        .type(ProviderConfigProperty.BOOLEAN_TYPE)
        .label("Every body")
        .helpText("Auto register user.")
        .defaultValue(true)
        .add()
        .property().name(WX_API_ID)
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("API ID")
        .helpText("Wei Xin APP API ID")
        .add()
        .property().name(WX_API_SECRET)
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("API secret")
        .helpText("Wei Xin APP API secret")
        .add()
        .build();
  }

  @Override
  public String getDisplayType() {
    return "WX APP auth";
  }

  @Override
  public String getReferenceCategory() {
    return "WX APP Grant";
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
    return true;
  }

  @Override
  public String getHelpText() {
    return "WX APP auth";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return CONFIG_PROPERTIES;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return new WXAppAuthenticator();
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
  public String getId() {
    return PROVIDER_ID;
  }
}
