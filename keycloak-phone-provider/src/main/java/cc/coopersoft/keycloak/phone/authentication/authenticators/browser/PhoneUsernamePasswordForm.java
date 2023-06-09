package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import cc.coopersoft.common.OptionalUtils;
import cc.coopersoft.keycloak.phone.Utils;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.List;

import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.*;
import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

public class PhoneUsernamePasswordForm extends UsernamePasswordForm implements Authenticator, AuthenticatorFactory {

  private static final Logger logger = Logger.getLogger(PhoneUsernamePasswordForm.class);

  public static final String PROVIDER_ID = "auth-phone-username-password-form";

  public static final String VERIFIED_PHONE_NUMBER = "LOGIN_BY_PHONE_VERIFY";

  private static final String CONFIG_IS_LOGIN_WITH_PHONE_VERIFY = "loginWithPhoneVerify";

  private static final String CONFIG_IS_LOGIN_WITH_PHONE_NUMBER = "loginWithPhoneNumber";

  /**
   * use phone and password login
   * @param context
   * @return
   */
  private boolean isLoginWithPhoneNumber(AuthenticationFlowContext context){
    return context.getAuthenticatorConfig() == null ||
        context.getAuthenticatorConfig().getConfig().getOrDefault(CONFIG_IS_LOGIN_WITH_PHONE_NUMBER, "true").equals("true");
  }

  /**
   * use phone and verify code login
   * @param context
   * @return
   */
  private boolean isSupportPhone(AuthenticationFlowContext context){
    return context.getAuthenticatorConfig() == null ||
        context.getAuthenticatorConfig().getConfig().getOrDefault(CONFIG_IS_LOGIN_WITH_PHONE_VERIFY, "true").equals("true");
  }

  private LoginFormsProvider assemblyForm(AuthenticationFlowContext context, LoginFormsProvider form){
    if (isSupportPhone(context))
      form.setAttribute(ATTRIBUTE_SUPPORT_PHONE, true);
    if (isLoginWithPhoneNumber(context)){
      form.setAttribute("loginWithPhoneNumber",true);
    }
    return form;
  }

  @Override
  protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    LoginFormsProvider forms = context.form();
    if (formData.size() > 0) forms.setFormData(formData);
    if (Utils.isDuplicatePhoneAllowed(context.getSession())) {
      forms.setError("duplicatePhoneAllowedCantLogin");
      logger.warn("duplicate phone allowed! phone login is disabled!");
    } else {
      forms = assemblyForm(context,forms);
    }
    return forms.createLoginUsernamePassword();
  }

  @Override
  protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {

    boolean byPhone = OptionalUtils
        .ofBlank(inputData.getFirst(FIELD_PATH_PHONE_ACTIVATED))
        .map(s -> "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
        .orElse(false);

    if (!byPhone) {
      return validateUserAndPassword(context, inputData);
    }
    String phoneNumber = inputData.getFirst(FIELD_PHONE_NUMBER);


    if (Validation.isBlank(phoneNumber)) {
      context.getEvent().error(Errors.USERNAME_MISSING);
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true);
      assemblyForm(context,context.form());
      Response challengeResponse = challenge(context, SupportPhonePages.Errors.MISSING.message(), FIELD_PHONE_NUMBER);
      context.forceChallenge(challengeResponse);
      return false;
    }

    String code = inputData.getFirst(FIELD_VERIFICATION_CODE);
    if (Validation.isBlank(code)) {
      invalidVerificationCode(context, phoneNumber);
      return false;
    }


    return validatePhone(context, phoneNumber, code.trim());
  }

  private void invalidVerificationCode(AuthenticationFlowContext context, String number) {

    context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
    context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
        .setAttribute(ATTEMPTED_PHONE_NUMBER, number);
    assemblyForm(context,context.form());
    Response challengeResponse = challenge(context, SupportPhonePages.Errors.NOT_MATCH.message(), FIELD_VERIFICATION_CODE);
    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
  }

  private boolean validatePhone(AuthenticationFlowContext context, String phoneNumber, String code) {

    context.clearUser();
    try {

     var validPhoneNumber = Utils.canonicalizePhoneNumber(context.getSession(),phoneNumber);

      return Utils.findUserByPhone(context.getSession(), context.getRealm(), validPhoneNumber)
          .map(user -> validateVerificationCode(context, user, validPhoneNumber, code) && validateUser(context, user, validPhoneNumber))
          .orElseGet(() -> {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
                .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
            assemblyForm(context,context.form());
            Response challengeResponse = challenge(context, SupportPhonePages.Errors.USER_NOT_FOUND.message(), FIELD_PHONE_NUMBER);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return false;
          });
    } catch (PhoneNumberInvalidException e) {
      context.getEvent().error(Errors.USERNAME_MISSING);
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
          .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
      assemblyForm(context,context.form());
      Response challengeResponse = challenge(context,e.getErrorType().message(), FIELD_PHONE_NUMBER);
      context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
      return false;
    }
  }

  private boolean validateVerificationCode(AuthenticationFlowContext context, UserModel user, String phoneNumber, String code) {
    try {
      context.getSession().getProvider(PhoneVerificationCodeProvider.class)
          .validateCode(user, phoneNumber, code, TokenCodeType.AUTH);
      logger.debug("verification code success!");
      return true;
    } catch (Exception e) {

      context.getEvent().user(user);
      invalidVerificationCode(context, phoneNumber);
      return false;
    }
  }

  private boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user, String phoneNumber) {
    String bruteForceError = getDisabledByBruteForceEventError(context, user);
    if (bruteForceError != null) {
      context.getEvent().user(user);
      context.getEvent().error(bruteForceError);
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
          .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
      assemblyForm(context,context.form());
      Response challengeResponse = challenge(context, disabledByBruteForceError(), disabledByBruteForceFieldError());
      context.forceChallenge(challengeResponse);
      return true;
    }
    return false;
  }

  private boolean enabledUser(AuthenticationFlowContext context, UserModel user, String phoneNumber) {
    if (isDisabledByBruteForce(context, user, phoneNumber)) return false;
    if (!user.isEnabled()) {
      context.getEvent().user(user);
      context.getEvent().error(Errors.USER_DISABLED);
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
          .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
      assemblyForm(context,context.form());
      Response challengeResponse = challenge(context, Messages.ACCOUNT_DISABLED);
      context.forceChallenge(challengeResponse);
      return false;
    }
    return true;
  }

  private boolean validateUser(AuthenticationFlowContext context, UserModel user, String phoneNumber) {
    if (!enabledUser(context, user, phoneNumber)) {
      return false;
    }
    context.getAuthenticationSession().setAuthNote(VERIFIED_PHONE_NUMBER, phoneNumber);
    context.setUser(user);
    return true;
  }

  private boolean validateUser(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {
    if (!enabledUser(context, user)) {
      return false;
    }
    String rememberMe = inputData.getFirst("rememberMe");
    boolean remember = rememberMe != null && rememberMe.equalsIgnoreCase("on");
    if (remember) {
      context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
      context.getEvent().detail(Details.REMEMBER_ME, "true");
    } else {
      context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
    }
    context.setUser(user);
    return true;
  }

  @Override
  public boolean validateUserAndPassword(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData)  {
    UserModel user = getUser(context, inputData);
    boolean shouldClearUserFromCtxAfterBadPassword = !isUserAlreadySetBeforeUsernamePasswordAuth(context);
    return user != null && validatePassword(context, user, inputData, shouldClearUserFromCtxAfterBadPassword) && validateUser(context, user, inputData);
  }

  private UserModel getUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
    if (isUserAlreadySetBeforeUsernamePasswordAuth(context)) {
      // Get user from the authentication context in case he was already set before this authenticator
      UserModel user = context.getUser();
      testInvalidUser(context, user);
      return user;
    } else {
      // Normal login. In this case this authenticator is supposed to establish identity of the user from the provided username
      context.clearUser();
      return getUserFromForm(context, inputData);
    }
  }

  private UserModel getUserFromForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
    String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
    if (username == null) {
      context.getEvent().error(Errors.USER_NOT_FOUND);
      Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
      assemblyForm(context,context.form());
      context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
      return null;
    }

    // remove leading and trailing whitespace
    username = username.trim();

    context.getEvent().detail(Details.USERNAME, username);
    context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

    UserModel user;
    try {
      user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
      if (user == null &&
          isLoginWithPhoneNumber(context) &&
          !Utils.isDuplicatePhoneAllowed(context.getSession())){
        user = Utils.findUserByPhone(context.getSession(), context.getRealm(), username).orElse(null);
      }
    } catch (ModelDuplicateException mde) {
      ServicesLogger.LOGGER.modelDuplicateException(mde);

      // Could happen during federation import
      if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
        setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
      } else {
        setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
      }
      return null;
    }

    testInvalidUser(context, user);
    return user;
  }

  @Override
  public String getDisplayType() {
    return "Phone Username Password Form";
  }

  @Override
  public String getReferenceCategory() {
    return PasswordCredentialModel.TYPE;
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

  protected static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

  static {
    CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property().name(CONFIG_IS_LOGIN_WITH_PHONE_VERIFY)
        .type(BOOLEAN_TYPE)
        .label("Login with phone verify")
        .helpText("Input phone number and password.  `Duplicate phone` must be false.")
        .defaultValue(true)
        .add()
        .property().name(CONFIG_IS_LOGIN_WITH_PHONE_NUMBER)
        .type(BOOLEAN_TYPE)
        .label("Login with phone number")
        .helpText("Input phone number and password.  `Duplicate phone` must be false.")
        .defaultValue(true)
        .add()
        .build();
  }
  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return CONFIG_PROPERTIES;
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
