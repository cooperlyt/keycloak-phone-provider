package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
import cc.coopersoft.keycloak.phone.utils.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

public class PhoneUsernamePasswordForm extends UsernamePasswordForm implements Authenticator, AuthenticatorFactory {

  private static final Logger logger = Logger.getLogger(PhoneUsernamePasswordForm.class);

  public static final String PROVIDER_ID = "auth-phone-username-password-form";
  private final String FORM_PHONE_VERIFICATION_CODE="code";

  private final String FORM_PHONE_NUMBER="phoneNumber";
  private final String FORM_ATTRIBUTE="loginByPhone";

  private final String FORM_ATTRIBUTE_INIT_PHONE_NUMBER="initPhoneNumber";

  private final String FORM_ATTRIBUTE_ACTIVE_PHONE ="activePhone";

  @Override
  protected Response createLoginForm(LoginFormsProvider form) {
    form.setAttribute(FORM_ATTRIBUTE, true);
    return form.createLoginUsernamePassword();
  }

  @Override
  protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    LoginFormsProvider forms = context.form();
    if (formData.size() > 0) forms.setFormData(formData);
    forms.setAttribute(FORM_ATTRIBUTE, true);
    return forms.createLoginUsernamePassword();
  }

  @Override
  protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {

    if (isUserAlreadySetBeforeUsernamePasswordAuth(context)){
      return validateUserAndPassword(context, inputData);
    }

    String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
    String phoneNumber = inputData.getFirst(FORM_PHONE_NUMBER);

    if (StringUtils.isBlank(username) && StringUtils.isBlank(phoneNumber)){
      context.getEvent().error(Errors.USER_NOT_FOUND);
      Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
      context.forceChallenge(challengeResponse);
      return false;
    }


    if(StringUtils.isNotBlank(username) || isUserAlreadySetBeforeUsernamePasswordAuth(context)){
      return  validateUserAndPassword(context, inputData);
    }

    String code = inputData.getFirst(FORM_PHONE_VERIFICATION_CODE);
    return validatePhone(context,phoneNumber,code);

  }

  private boolean validatePhone(AuthenticationFlowContext context, String phoneNumber, String code){
    context.clearUser();
    if (StringUtils.isBlank(phoneNumber)){
      context.getEvent().error(Errors.USER_NOT_FOUND);
      context.form().setAttribute(FORM_ATTRIBUTE_ACTIVE_PHONE, true);
      Response challengeResponse = challenge(context,"requiredPhoneNumber" , FORM_PHONE_NUMBER);
      context.forceChallenge(challengeResponse);
      return false;
    }

    UserModel user = UserUtils.findUserByPhone(context.getSession().users(), context.getRealm(), phoneNumber.trim());
    if (user == null) {
      context.getEvent().error(Errors.USER_NOT_FOUND);
      context.form().setAttribute(FORM_ATTRIBUTE_ACTIVE_PHONE, true);
      context.form().setAttribute(FORM_ATTRIBUTE_INIT_PHONE_NUMBER,phoneNumber);
      Response challengeResponse = challenge(context,"phoneUserNotFound" , FORM_PHONE_NUMBER);
      context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
    }
    return user != null && validateVerificationCode(context,user,phoneNumber,code) && validateUser(context,user);
  }

  private boolean validateVerificationCode(AuthenticationFlowContext context,UserModel user, String phoneNumber, String code) {

    if (StringUtils.isBlank(code)){
      context.getEvent().user(user);
      context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
      context.form().setAttribute(FORM_ATTRIBUTE_ACTIVE_PHONE, true);
      context.form().setAttribute(FORM_ATTRIBUTE_INIT_PHONE_NUMBER,phoneNumber);
      Response challengeResponse = challenge(context, "verificationCodeDoesNotMatch", FORM_PHONE_VERIFICATION_CODE);
      context.forceChallenge(challengeResponse);
      return false;
    }

    try {
      context.getSession().getProvider(TokenCodeService.class).validateCode(user, phoneNumber, code, TokenCodeType.OTP);
      logger.debug("verification code success!");
      return true;
    } catch (Exception e) {
      context.getEvent().user(user);
      context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
      context.form().setAttribute(FORM_ATTRIBUTE_ACTIVE_PHONE, true);
      context.form().setAttribute(FORM_ATTRIBUTE_INIT_PHONE_NUMBER,phoneNumber);
      Response challengeResponse = challenge(context, "verificationCodeDoesNotMatch", FORM_PHONE_VERIFICATION_CODE);
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
      return false;
    }
  }

  private boolean validateUser(AuthenticationFlowContext context,UserModel user){
    if (!enabledUser(context, user)) {
      return false;
    }
    context.setUser(user);
    return true;
  }

  @Override
  public String getDisplayType() {
    return "Phone Username Password Form";
  }

  @Override
  public String getReferenceCategory() {
    return PasswordCredentialModel.TYPE;
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
      AuthenticationExecutionModel.Requirement.REQUIRED
  };

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
    return "Validates a username and password or phone and verification code from login form.";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return this;
  }

  @Override
  public void init(Config.Scope config) {

  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {

  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
