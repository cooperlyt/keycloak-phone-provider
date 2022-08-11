package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
import cc.coopersoft.keycloak.phone.utils.OptionalStringUtils;
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
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.*;
import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

public class PhoneUsernamePasswordForm extends UsernamePasswordForm implements Authenticator, AuthenticatorFactory {

  private static final Logger logger = Logger.getLogger(PhoneUsernamePasswordForm.class);

  public static final String PROVIDER_ID = "auth-phone-username-password-form";

  @Override
  protected Response createLoginForm(LoginFormsProvider form) {
    form.setAttribute(ATTRIBUTE_SUPPORT_PHONE, true);
    return form.createLoginUsernamePassword();
  }

  @Override
  protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    LoginFormsProvider forms = context.form();
    if (formData.size() > 0) forms.setFormData(formData);
    forms.setAttribute(ATTRIBUTE_SUPPORT_PHONE, true);
    return forms.createLoginUsernamePassword();
  }

  @Override
  protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {

    boolean byPhone = OptionalStringUtils
        .ofBlank(inputData.getFirst(FIELD_PATH_PHONE_ACTIVATED))
        .map(s -> "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
        .orElse(false);

    if (!byPhone) {
      return validateUserAndPassword(context, inputData);
    }
    String phoneNumber = inputData.getFirst(FIELD_PHONE_NUMBER);


    if (StringUtils.isBlank(phoneNumber)){
      context.getEvent().error(Errors.USERNAME_MISSING);
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true);
      Response challengeResponse = challenge(context, SupportPhonePages.Errors.MISSING.message(), FIELD_PHONE_NUMBER);
      context.forceChallenge(challengeResponse);
      return false;
    }
    phoneNumber = phoneNumber.trim();

    String code = inputData.getFirst(FIELD_VERIFICATION_CODE);
    if (StringUtils.isBlank(code)){
      invalidVerificationCode(context,phoneNumber);
      return false;
    }

    return validatePhone(context,phoneNumber,code.trim());
  }

  private void invalidVerificationCode(AuthenticationFlowContext context, String number) {
    context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
    context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
        .setAttribute(ATTEMPTED_PHONE_NUMBER, number);
    Response challengeResponse = challenge(context, MESSAGE_VERIFICATION_CODE_NOT_MATCH, FIELD_VERIFICATION_CODE);
    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
  }

  private boolean validatePhone(AuthenticationFlowContext context, String phoneNumber, String code) {
    context.clearUser();
    return UserUtils.findUserByPhone(context.getSession().users(), context.getRealm(), phoneNumber)
        .map(user -> validateVerificationCode(context, user, phoneNumber, code) && validateUser(context, user,phoneNumber))
        .orElseGet(() -> {
          context.getEvent().error(Errors.USER_NOT_FOUND);
          context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
            .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
          Response challengeResponse = challenge(context, MESSAGE_PHONE_USER_NOT_FOUND, FIELD_PHONE_NUMBER);
          context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
          return false;
        });
  }

  private boolean validateVerificationCode(AuthenticationFlowContext context, UserModel user, String phoneNumber, String code) {
    try {
      context.getSession().getProvider(TokenCodeService.class).validateCode(user, phoneNumber, code, TokenCodeType.AUTH);
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
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED,true)
          .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
      Response challengeResponse = challenge(context, disabledByBruteForceError(), disabledByBruteForceFieldError());
      context.forceChallenge(challengeResponse);
      return true;
    }
    return false;
  }

  private boolean enabledUser(AuthenticationFlowContext context, UserModel user, String phoneNumber) {
    if (isDisabledByBruteForce(context, user,phoneNumber)) return false;
    if (!user.isEnabled()) {
      context.getEvent().user(user);
      context.getEvent().error(Errors.USER_DISABLED);
      context.form().setAttribute(ATTEMPTED_PHONE_ACTIVATED,true)
          .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber);
      Response challengeResponse = challenge(context, Messages.ACCOUNT_DISABLED);
      context.forceChallenge(challengeResponse);
      return false;
    }
    return true;
  }
  private boolean validateUser(AuthenticationFlowContext context, UserModel user, String phoneNumber) {
    if (!enabledUser(context, user,phoneNumber)) {
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
