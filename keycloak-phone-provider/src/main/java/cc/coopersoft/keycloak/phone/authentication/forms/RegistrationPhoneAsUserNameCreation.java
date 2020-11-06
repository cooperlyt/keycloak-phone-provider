package cc.coopersoft.keycloak.phone.authentication.forms;

import cc.coopersoft.keycloak.phone.utils.UserUtils;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationUserCreation;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

/**
 * replace  org.keycloak.authentication.forms.RegistrationUserCreation.java
 */
public class RegistrationPhoneAsUserNameCreation implements FormActionFactory, FormAction {

    private static final Logger logger = Logger.getLogger(RegistrationPhoneAsUserNameCreation.class);

    public static final String PROVIDER_ID = "registration-phone-username-creation";

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };


    @Override
    public String getDisplayType() {
        return "Registration Phone As Username Creation";
    }

    @Override
    public String getHelpText() {
        return "This action must always be first And Do not use Email as username! registration phone number as username. In success phase, this will create the user in the database.";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
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
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
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
        loginFormsProvider.setAttribute("registrationPhoneAsUsername", true);
    }

    @Override
    public void validate(ValidationContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        String phoneNumber = formData.getFirst(RegistrationPhoneNumber.FIELD_PHONE_NUMBER);
        context.getEvent().detail(Details.USERNAME, phoneNumber);

        if (Validation.isBlank(phoneNumber)){
            errors.add(new FormMessage(RegistrationPhoneNumber.FIELD_PHONE_NUMBER, RegistrationPhoneNumber.MISSING_PHONE_NUMBER));
            context.error(Errors.INVALID_REGISTRATION);
            context.validationError(formData, errors);
            return;
        }

        if (!UserUtils.isDuplicatePhoneAllowed() && UserUtils.findUserByPhone(context.getSession().users(),context.getRealm(),phoneNumber) != null) {
            context.error(Errors.INVALID_REGISTRATION);
            formData.remove(RegistrationPhoneNumber.FIELD_PHONE_NUMBER);
            errors.add(new FormMessage(RegistrationPhoneNumber.FIELD_PHONE_NUMBER, RegistrationPhoneNumber.PHONE_EXISTS));
            context.validationError(formData, errors);
            return;
        }

        if (context.getSession().users().getUserByUsername(phoneNumber, context.getRealm()) != null) {
            context.error(Errors.USERNAME_IN_USE);
            errors.add(new FormMessage(RegistrationPhoneNumber.FIELD_PHONE_NUMBER, Messages.USERNAME_EXISTS));
            formData.remove(RegistrationPhoneNumber.FIELD_PHONE_NUMBER);
            context.validationError(formData, errors);
            return;
        }

        context.success();
    }

    @Override
    public void success(FormContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String username = formData.getFirst(RegistrationPhoneNumber.FIELD_PHONE_NUMBER);

        context.getEvent().detail(Details.USERNAME, username)
                .detail(Details.REGISTER_METHOD, "form");
        UserModel user = context.getSession().users().addUser(context.getRealm(), username);
        user.setEnabled(true);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);
        AttributeFormDataProcessor.process(formData, context.getRealm(), user);
        context.setUser(user);
        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        context.getEvent().client(context.getAuthenticationSession().getClient().getClientId())
                .detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
                .detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
        String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
        if (authType != null) {
            context.getEvent().detail(Details.AUTH_TYPE, authType);
        }

        logger.info(String.format("user: %s is created, user name is %s ",user.getId(), user.getUsername()));
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return !realmModel.isRegistrationEmailAsUsername();
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }
}
