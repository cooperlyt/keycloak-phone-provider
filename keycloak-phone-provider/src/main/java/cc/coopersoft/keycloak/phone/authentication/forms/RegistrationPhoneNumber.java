/**
 * add by zhangzhl
 * 2020-07-27
 * 注册页手机号码必填验证
 * 
 */
package cc.coopersoft.keycloak.phone.authentication.forms;

import cc.coopersoft.keycloak.phone.utils.UserUtils;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.spi.TokenCodeService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class RegistrationPhoneNumber implements FormAction, FormActionFactory {

	private static final Logger logger = Logger.getLogger(RegistrationPhoneNumber.class);

	public static final String PROVIDER_ID = "registration-phone";
	public static final String FIELD_PHONE_NUMBER = "phoneNumber";
	public static final String FIELD_VERIFICATION_CODE = "registerCode";

	public static final String MISSING_PHONE_NUMBER = "requiredPhoneNumber";
	public static final String PHONE_EXISTS = "phoneNumberExists";

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

	private TokenCodeService getTokenCodeService(KeycloakSession session){
		return session.getProvider(TokenCodeService.class);
	}

	@Override
	public void validate(ValidationContext context) {


		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		List<FormMessage> errors = new ArrayList<>();
		context.getEvent().detail(Details.REGISTER_METHOD, "form");

		KeycloakSession session = context.getSession();

		String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);
		context.getEvent().detail(FIELD_PHONE_NUMBER, phoneNumber);

		if (Validation.isBlank(phoneNumber)) {
			context.error(Errors.INVALID_REGISTRATION);
			errors.add(new FormMessage(FIELD_PHONE_NUMBER, MISSING_PHONE_NUMBER));
			context.validationError(formData, errors);
			return;
		}

		if (!UserUtils.isDuplicatePhoneAllowed() && UserUtils.findUserByPhone(session.users(),context.getRealm(),phoneNumber) != null) {
			formData.remove(FIELD_PHONE_NUMBER);
			context.getEvent().detail(FIELD_PHONE_NUMBER, phoneNumber);
			errors.add(new FormMessage(FIELD_PHONE_NUMBER, PHONE_EXISTS));
			context.error(Errors.INVALID_REGISTRATION);
			context.validationError(formData, errors);
			return;
		}

		String verificationCode = formData.getFirst(FIELD_VERIFICATION_CODE);
		TokenCodeRepresentation tokenCode =  getTokenCodeService(session).ongoingProcess(phoneNumber, TokenCodeType.REGISTRATION);
		if (Validation.isBlank(verificationCode) || tokenCode == null || !tokenCode.getCode().equals(verificationCode)){
			context.error(Errors.INVALID_REGISTRATION);
			formData.remove(FIELD_VERIFICATION_CODE);
			errors.add(new FormMessage(FIELD_VERIFICATION_CODE, "verificationCodeDoesNotMatch"));
			context.validationError(formData, errors);
			return;
		}

		context.getSession().setAttribute("tokenId",tokenCode.getId());
		context.success();
	}

	@Override
	public void success(FormContext context) {

		UserModel user = context.getUser();

		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);
		String tokenId = context.getSession().getAttribute("tokenId",String.class);

		logger.info(String.format("registration user %s phone success, tokenId is: %s", user.getId(), tokenId));
		getTokenCodeService(context.getSession()).tokenValidated(user,phoneNumber,tokenId);
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
