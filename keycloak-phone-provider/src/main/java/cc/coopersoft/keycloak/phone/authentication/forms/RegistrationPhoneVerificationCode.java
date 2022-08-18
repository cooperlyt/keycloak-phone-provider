/**
 * add by zhangzhl
 * 2020-07-27
 * 注册页手机号码必填验证
 */
package cc.coopersoft.keycloak.phone.authentication.forms;

import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
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
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.*;

public class RegistrationPhoneVerificationCode implements FormAction, FormActionFactory {

  private static final Logger logger = Logger.getLogger(RegistrationPhoneVerificationCode.class);

  public static final String PROVIDER_ID = "registration-phone";


  @Override
  public String getHelpText() {
    return "valid phone number and verification code";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
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
    return false;
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
    context.getEvent().detail(FIELD_PHONE_NUMBER, phoneNumber);


    String verificationCode = formData.getFirst(FIELD_VERIFICATION_CODE);
    TokenCodeRepresentation tokenCode = getTokenCodeService(session).ongoingProcess(phoneNumber, TokenCodeType.REGISTRATION);
    if (Validation.isBlank(verificationCode) || tokenCode == null || !tokenCode.getCode().equals(verificationCode)) {
      context.error(Errors.INVALID_REGISTRATION);
      formData.remove(FIELD_VERIFICATION_CODE);
      errors.add(new FormMessage(FIELD_VERIFICATION_CODE, "verificationCodeDoesNotMatch"));
      context.validationError(formData, errors);
      return;
    }

    context.getSession().setAttribute("tokenId", tokenCode.getId());
    context.success();
  }

  @Override
  public void success(FormContext context) {

    UserModel user = context.getUser();

    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);
    String tokenId = context.getSession().getAttribute("tokenId", String.class);

    logger.info(String.format("registration user %s phone success, tokenId is: %s", user.getId(), tokenId));
    getTokenCodeService(context.getSession()).tokenValidated(user, phoneNumber, tokenId);

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
