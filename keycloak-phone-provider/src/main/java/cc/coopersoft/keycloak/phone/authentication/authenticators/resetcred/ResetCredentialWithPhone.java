package cc.coopersoft.keycloak.phone.authentication.authenticators.resetcred;

import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.common.OptionalUtils;
import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.*;
import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;

public class ResetCredentialWithPhone implements Authenticator, AuthenticatorFactory {

  private static final Logger logger = Logger.getLogger(ResetCredentialWithPhone.class);

  public static final String PROVIDER_ID = "reset-credentials-with-phone";

  public static final String SHOULD_SEND_EMAIL = "should-send-email";

  @Override
  public void authenticate(AuthenticationFlowContext context) {

    String existingUserId = context.getAuthenticationSession().getAuthNote(AbstractIdpAuthenticator.EXISTING_USER_INFO);
    if (existingUserId != null) {
      UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession());

      logger.debugf("Forget-password triggered when reauthenticating user after first broker login. Prefilling reset-credential-choose-user screen with user '%s' ", existingUser.getUsername());
      context.setUser(existingUser);
      Response challenge = context.form().createPasswordReset();
      context.challenge(challenge);
      return;
    }

    String actionTokenUserId = context.getAuthenticationSession().getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
    if (actionTokenUserId != null) {
      UserModel existingUser = context.getSession().users().getUserById(context.getRealm(), actionTokenUserId);

      // Action token logics handles checks for user ID validity and user being enabled

      logger.debugf("Forget-password triggered when reauthenticating user after authentication via action token. Skipping reset-credential-choose-user screen and using user '%s' ", existingUser.getUsername());
      context.setUser(existingUser);
      context.success();
      return;
    }
    context.challenge(challenge(context));
  }

  protected Response challenge(AuthenticationFlowContext context) {
    return context.form()
        .setAttribute(ATTRIBUTE_SUPPORT_PHONE, true)
        .createPasswordReset();
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

    if (!validateForm(context, formData)) {
      return;
    }
    context.success();
  }



  protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
    boolean byPhone = OptionalUtils
        .ofBlank(inputData.getFirst(FIELD_PATH_PHONE_ACTIVATED))
        .map(s -> "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
        .orElse(false);

    context.clearUser();

    String phoneNumber = inputData.getFirst(FIELD_PHONE_NUMBER);
    String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);

    UserModel user;
    if (!byPhone){
      if (Validation.isBlank(username)) {
        context.getEvent().error(Errors.USERNAME_MISSING);
        Response challenge = challenge(context, Validation.FIELD_USERNAME, Messages.MISSING_USERNAME);
        context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
        return false;
      }
      user = getUserByUsername(context, username.trim());
    }else{

      if (Validation.isBlank(phoneNumber)) {
        context.getEvent().error(Errors.USERNAME_MISSING);
        Response challenge = challenge(context, FIELD_PHONE_NUMBER, SupportPhonePages.Errors.MISSING.message(), phoneNumber);
        context.forceChallenge(challenge);
        return false;
      }

      try {
        phoneNumber = Utils.canonicalizePhoneNumber(context.getSession(),phoneNumber);
      } catch (PhoneNumberInvalidException e) {
        context.getEvent().error(Errors.USERNAME_MISSING);
        Response challenge = challenge(context, FIELD_PHONE_NUMBER,
            e.getErrorType().message(), phoneNumber);
        context.forceChallenge(challenge);
        return false;
      }


      String verificationCode = inputData.getFirst(FIELD_VERIFICATION_CODE);
      if (Validation.isBlank(verificationCode)) {
        invalidVerificationCode(context, phoneNumber);
        return false;
      }
      user = Utils.findUserByPhone(context.getSession(), context.getRealm(), phoneNumber)
          .orElse(null);

      if (user != null && !validateVerificationCode(context, user, phoneNumber, verificationCode.trim())){
        return false;
      }
    }

    return validateUser(context,user,byPhone,byPhone ? phoneNumber : username);
  }

  protected UserModel getUserByUsername(AuthenticationFlowContext context, String username) {
    RealmModel realm = context.getRealm();
    UserModel user = context.getSession().users().getUserByUsername(realm, username);
    if (user == null && realm.isLoginWithEmailAllowed() && username.contains("@")) {
      user = context.getSession().users().getUserByEmail(realm, username);
    }
    context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
    return user;
  }

  protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user, String phoneNumber) {
    String bruteForceError = getDisabledByBruteForceEventError(context, user);
    if (bruteForceError != null) {
      context.getEvent().user(user);
      context.getEvent().error(bruteForceError);
      Response challenge = challenge(context, FIELD_PHONE_NUMBER, Messages.INVALID_USER, phoneNumber);
      context.forceChallenge(challenge);
      return true;
    }
    return false;
  }

  protected boolean isDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
    String bruteForceError = getDisabledByBruteForceEventError(context, user);
    if (bruteForceError != null) {
      context.getEvent().user(user);
      context.getEvent().error(bruteForceError);
      Response challenge = challenge(context, FIELD_PHONE_NUMBER, Messages.INVALID_USER);
      context.forceChallenge(challenge);
      return true;
    }
    return false;
  }

  protected boolean validateUser(AuthenticationFlowContext context,
                                 UserModel user, boolean byPhone, String attempted) {

    if (user == null){
      context.getEvent().error(Errors.USER_NOT_FOUND);
      if(!byPhone){
        context.getEvent().detail(Details.USERNAME, attempted);
      }
      Response challenge = byPhone ? challenge(context, FIELD_PHONE_NUMBER, SupportPhonePages.Errors.USER_NOT_FOUND.message(), attempted) : challenge(context, Validation.FIELD_USERNAME, Messages.INVALID_USERNAME_OR_EMAIL);
      context.forceChallenge(challenge);
      return false;
    }

    if (byPhone ? isDisabledByBruteForce(context, user, attempted) : isDisabledByBruteForce(context, user)) return false;
    if (!user.isEnabled()) {
      if(!byPhone){
        context.getEvent().detail(Details.USERNAME, attempted);
      }
      context.getEvent().user(user);
      context.getEvent().error(Errors.USER_DISABLED);
      Response challenge = byPhone ? challenge(context,FIELD_PHONE_NUMBER, Errors.USER_DISABLED, attempted) : challenge(context,Validation.FIELD_USERNAME,Errors.USER_DISABLED);
      context.forceChallenge(challenge);
      return false;
    }

    if (byPhone){
      context.getAuthenticationSession().setAuthNote(SHOULD_SEND_EMAIL, "false");
    }
    context.setUser(user);
    return true;
  }

  protected void invalidVerificationCode(AuthenticationFlowContext context, String phoneNumber) {
    context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
    Response challenge = challenge(context, FIELD_VERIFICATION_CODE, SupportPhonePages.Errors.NOT_MATCH.message(), phoneNumber);
    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
  }

  protected Response challenge(AuthenticationFlowContext context,
                               String field, String message,
                               String phoneNumber) {
    return context.form()
        .addError(new FormMessage(field, message))
        .setAttribute(ATTRIBUTE_SUPPORT_PHONE, true)
        .setAttribute(ATTEMPTED_PHONE_ACTIVATED, true)
        .setAttribute(ATTEMPTED_PHONE_NUMBER, phoneNumber)
        .createPasswordReset();
  }

  protected Response challenge(AuthenticationFlowContext context,
                               String field, String message) {
    return context.form()
        .addError(new FormMessage(field, message))
        .setAttribute(ATTRIBUTE_SUPPORT_PHONE, true)
        .createPasswordReset();
  }

  private boolean validateVerificationCode(AuthenticationFlowContext context, UserModel user, String phoneNumber, String code) {
    try {
      context.getSession().getProvider(PhoneVerificationCodeProvider.class).validateCode(user, phoneNumber, code, TokenCodeType.RESET);
      logger.debug("verification code success!");
      return true;
    } catch (Exception e) {
      logger.debug("verification code fail!");
      context.getEvent().user(user);
      invalidVerificationCode(context, phoneNumber);
      return false;
    }
  }


  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

  }


  @Override
  public String getDisplayType() {
    return "Reset Credential Choose User with Phone";
  }

  @Override
  public String getReferenceCategory() {
    return null;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
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
    return "Choose a user to reset credentials for";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
  }

  @Override
  public void close() {

  }
}
