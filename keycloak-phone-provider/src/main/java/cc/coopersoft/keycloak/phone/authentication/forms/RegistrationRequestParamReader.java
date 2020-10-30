package cc.coopersoft.keycloak.phone.authentication.forms;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistrationRequestParamReader implements  FormActionFactory, FormAction {

    private static final Logger logger = Logger.getLogger(RegistrationRequestParamReader.class);


    public static final String PROVIDER_ID = "registration-http-request-param";

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };

    private static String[] QUERY_PARAM_BLACKLIST = {
            "execution",
            "session_code",
            "client_id",
            "tab_id",
            "response_type",
            "scope",
            "redirect_uri"
    };

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
        MultivaluedMap<String, String> formData = validationContext.getHttpRequest().getDecodedFormParameters();
        validationContext.validationError(formData, new ArrayList<>());
        validationContext.success();
    }

    @Override
    public void success(FormContext context) {

        MultivaluedMap<String, String> parameters = context.getUriInfo().getQueryParameters();
        UserModel user = context.getUser();
        parameters.forEach((k,v) -> {
            if (Arrays.stream(QUERY_PARAM_BLACKLIST).noneMatch(item -> item.equals(k))){
                if (v.size() > 1){
                    user.setAttribute(k,v);
                }else if (!v.isEmpty()){
                    user.setSingleAttribute(k,v.stream().findFirst().orElseThrow());
                }
            }
        });

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
