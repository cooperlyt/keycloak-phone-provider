/**
 * add by zhangzhl
 * 2020-07-27
 * 注册页手机号码必填验证
 * 
 */
package cc.coopersoft.keycloak.registration.sms;

import cc.coopersoft.keycloak.UserUtil;
import cc.coopersoft.keycloak.authenticators.sms.PhoneNumberAuthenticator;
import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import cc.coopersoft.keycloak.providers.sms.spi.TokenCodeService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class RegistrationPhoneNumber implements FormAction, FormActionFactory {

	private static final Logger logger = Logger.getLogger(RegistrationPhoneNumber.class);

	public static final String PROVIDER_ID = "registration-phone";
	public static final String FIELD_PHONE_NUMBER = "phoneNumber";
	public static final String FIELD_VERIFICATION_CODE = "registerCode";

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

	private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
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
	@Override
	public void validate(ValidationContext context) {


		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		KeycloakSession session = context.getSession();
		List<FormMessage> errors = new ArrayList<>();
		context.getEvent().detail(Details.REGISTER_METHOD, "form");
		String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);
		if (Validation.isBlank(phoneNumber)) {
			errors.add(new FormMessage(FIELD_PHONE_NUMBER, "requiredPhoneNumber"));
		} else {

			boolean exists = UserUtil.findUserByPhone(session.users(),context.getRealm(),phoneNumber, context.getUser().getId()) != null;


			if (exists) {
				errors.add(new FormMessage(FIELD_PHONE_NUMBER, "phoneNumberExists"));
			}
		}

		String verificationCode = formData.getFirst(FIELD_VERIFICATION_CODE);
		if (Validation.isBlank(verificationCode)){
			errors.add(new FormMessage(FIELD_PHONE_NUMBER, "requiredVerificationCode"));
		} else {
			try {
				context.getSession().getProvider(TokenCodeService.class).validateCode(context.getUser(), phoneNumber, verificationCode, TokenCodeType.REGISTRATION);
			} catch (Exception e) {
				errors.add(new FormMessage(FIELD_VERIFICATION_CODE, "verificationCodeDoesNotMatch"));
			}
		}

		if (errors.size() > 0) {
			context.error(Errors.INVALID_REGISTRATION);
			context.validationError(formData, errors);
			return;
		} else {
			context.success();
		}
	}

	@Override
	public void success(FormContext context) {

//		UserModel user = context.getUser();
//		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
//		user.setSingleAttribute("phoneNumberVerified", "true");
//		user.setSingleAttribute("phoneNumber", formData.getFirst(FIELD_PHONE_NUMBER));
	}

	@Override
	public void buildPage(FormContext context, LoginFormsProvider form) {
		form.setAttribute("phoneNumberRequired", true);
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
