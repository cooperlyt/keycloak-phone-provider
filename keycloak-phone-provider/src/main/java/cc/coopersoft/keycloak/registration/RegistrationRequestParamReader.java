package cc.coopersoft.keycloak.registration;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class RegistrationRequestParamReader implements  FormActionFactory, FormAction {

    private static final Logger logger = Logger.getLogger(RegistrationRequestParamReader.class);


    public static final String PROVIDER_ID = "registration-http-request-param";

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };


    @Override
    public String getDisplayType() {
        return "Request param reader";
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
    public String getHelpText() {
        return "Read http request param add to user attribute";
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

    }

    @Override
    public void validate(ValidationContext validationContext) {

    }

    @Override
    public void success(FormContext formContext) {
        logger.info("registration request reader success!");
        logger.info(String.format("request reader uri : %s",formContext.getHttpRequest().getUri()));
        formContext.getHttpRequest().getDecodedFormParameters().keySet().forEach(v -> logger.info(String.format("request reader form param: %s" , v)));

        formContext.getHttpRequest().getAttributeNames().asIterator().forEachRemaining(it -> logger.info(String.format("request reader attr param: %s -> %s",it, formContext.getHttpRequest().getAttribute(it))));
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
