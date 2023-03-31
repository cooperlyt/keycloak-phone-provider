package cc.coopersoft.keycloak.phone.authentication.forms;

import okhttp3.HttpUrl;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.keycloak.provider.ProviderConfigProperty.MULTIVALUED_STRING_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

public class RegistrationRedirectParametersReader implements FormActionFactory, FormAction {

  private static final Logger logger = Logger.getLogger(RegistrationRedirectParametersReader.class);

  public static final String PROVIDER_ID = "registration-redirect-parameter";
  public static final String PARAM_NAMES = "acceptParameter";


  private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED};


  @Override
  public String getDisplayType() {
    return "Redirect parameter reader";
  }

  @Override
  public String getReferenceCategory() {
    return null;
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    ProviderConfigProperty rep =
        new ProviderConfigProperty(PARAM_NAMES,
            "Accept query param",
            "Registration query param accept names.",
            MULTIVALUED_STRING_TYPE, null);
    return Collections.singletonList(rep);
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
    return "Read query parameter add to user attribute";
  }

  @Override
  public FormAction create(KeycloakSession session) {
    return this;
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

  // FormAction

  @Override
  public void buildPage(FormContext formContext, LoginFormsProvider loginFormsProvider) {
  }

  @Override
  public void validate(ValidationContext validationContext) {
    validationContext.success();
  }

  @Override
  public void success(FormContext context) {


    String redirectUri = context.getAuthenticationSession().getRedirectUri();
    logger.info("add user attribute form redirectUri:" + redirectUri);
    if (Validation.isBlank(redirectUri)) {
      logger.error("no referer. cant get param in keycloak version");
      return;
    }

    HttpUrl url = HttpUrl.parse(redirectUri);
    if (url == null) {
      logger.error("redirectUri is null");
      return;
    }
    //url.queryParameterNames().forEach(s -> logger.info("redirect param name ->" + s));
    UserModel user = context.getUser();
    AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();

    if (authenticatorConfig == null || authenticatorConfig.getConfig() == null) {
      logger.error("can't get config!");
      return;
    }

    String params = authenticatorConfig.getConfig().get(PARAM_NAMES);

    if (Validation.isBlank(params)) {
      logger.warn("accept params is not configure.");
      return;
    }

    logger.info("allow query param names:" + params);

    List<String> finalParamNames = new ArrayList<>();

    Pattern p = Pattern.compile("[A-Za-z_]\\w*");
    Matcher m = p.matcher(params);
    while (m.find()) {
      finalParamNames.add(m.group());
    }

    if (finalParamNames.isEmpty()) {
      logger.warn("accept params is not configure.");
      return;
    }

    url.queryParameterNames()
        .stream()
        .filter(finalParamNames::contains)
        .forEach(v -> user.setAttribute(v, url.queryParameterValues(v)));

  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

  }
}
