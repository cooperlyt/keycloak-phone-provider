package cc.coopersoft.keycloak.phone.authentication.forms;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.*;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

public class RegistrationPhoneVerificationCode implements FormAction, FormActionFactory {

  private static final Logger logger = Logger.getLogger(RegistrationPhoneVerificationCode.class);

  public static final String PROVIDER_ID = "registration-phone";

  public static final String CONFIG_OPT_CREDENTIAL="createOPTCredential";

  @Override
  public String getHelpText() {
    return "valid phone number and verification code";
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public void close() {

  }

  @Override
  public String getDisplayType() {
    return "Phone validation";
  }

  @Override
  public String getReferenceCategory() {
    return null;
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  protected static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

  static {
    CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property().name(CONFIG_OPT_CREDENTIAL)
        .type(BOOLEAN_TYPE)
        .defaultValue(false)
        .label("Create OTP Credential")
        .helpText("Create OTP credential by phone number.")
        .add()
        .build();
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return CONFIG_PROPERTIES;
  }

  private final static Requirement[] REQUIREMENT_CHOICES = {
      Requirement.REQUIRED, Requirement.DISABLED};

  @Override
  public Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
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
  public String getId() {
    return PROVIDER_ID;
  }


  // FormAction

  private PhoneVerificationCodeProvider getTokenCodeService(KeycloakSession session) {
    return session.getProvider(PhoneVerificationCodeProvider.class);
  }

  @Override
  public void validate(ValidationContext context) {


    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    List<FormMessage> errors = new ArrayList<>();
    context.getEvent().detail(Details.REGISTER_METHOD, "form");

    KeycloakSession session = context.getSession();

    String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);

    if (Validation.isBlank(phoneNumber)){
      context.error(Errors.INVALID_REGISTRATION);
      errors.add(new FormMessage(FIELD_PHONE_NUMBER, SupportPhonePages.Errors.MISSING));
      context.validationError(formData, errors);
      return;
    }

    try {
      phoneNumber = Utils.canonicalizePhoneNumber(context.getSession(),phoneNumber);
    } catch (PhoneNumberInvalidException e) {
      context.error(Errors.INVALID_REGISTRATION);
      errors.add(new FormMessage(FIELD_PHONE_NUMBER, e.getErrorType().message()));
      context.validationError(formData, errors);
      return;
    }

    context.getEvent().detail(FIELD_PHONE_NUMBER, phoneNumber);

    String verificationCode = formData.getFirst(FIELD_VERIFICATION_CODE);
    TokenCodeRepresentation tokenCode = getTokenCodeService(session).ongoingProcess(phoneNumber, TokenCodeType.REGISTRATION);
    if (Validation.isBlank(verificationCode) || tokenCode == null || !tokenCode.getCode().equals(verificationCode)) {
      context.error(Errors.INVALID_REGISTRATION);
      formData.remove(FIELD_VERIFICATION_CODE);
      errors.add(new FormMessage(FIELD_VERIFICATION_CODE, SupportPhonePages.Errors.NOT_MATCH.message()));
      context.validationError(formData, errors);
      return;
    }

    context.getSession().setAttribute("tokenId", tokenCode.getId());
    context.success();
  }

  @Override
  public void success(FormContext context) {

    UserModel user = context.getUser();
    var session = context.getSession();

    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

    String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);

    try {
      phoneNumber = Utils.canonicalizePhoneNumber(context.getSession(),phoneNumber);
    } catch (PhoneNumberInvalidException e) {
      //verified in validate process
      throw new IllegalStateException();
    }

    String tokenId = session.getAttribute("tokenId", String.class);

    logger.info(String.format("registration user %s phone success, tokenId is: %s", user.getId(), tokenId));
    getTokenCodeService(context.getSession()).tokenValidated(user, phoneNumber, tokenId,false);

    AuthenticatorConfigModel config = context.getAuthenticatorConfig();
    if (config != null &&
        "true".equalsIgnoreCase(config.getConfig().getOrDefault(CONFIG_OPT_CREDENTIAL,"false"))){
      PhoneOtpCredentialProvider ocp = (PhoneOtpCredentialProvider) context.getSession()
          .getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);
      ocp.createCredential(context.getRealm(), context.getUser(), PhoneOtpCredentialModel.create(phoneNumber,tokenId,0));
    }

  }

  @Override
  public void buildPage(FormContext context, LoginFormsProvider form) {
    form.setAttribute("verifyPhone", true);
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
}
