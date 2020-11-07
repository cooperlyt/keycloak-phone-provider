package cc.coopersoft.keycloak.phone.authentication.forms;

import okhttp3.HttpUrl;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RegistrationRedirectParametersReader implements  FormActionFactory, FormAction {

    private static final Logger logger = Logger.getLogger(RegistrationRedirectParametersReader.class);

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    public static final String PROVIDER_ID = "registration-redirect-parameter";

    public static final String PARAM_NAMES = "registration.parameter.accept";

    static {
        ProviderConfigProperty acceptParamName;
        acceptParamName = new ProviderConfigProperty();
        acceptParamName.setName(PARAM_NAMES);
        acceptParamName.setLabel("Accept query param");
        acceptParamName.setType(ProviderConfigProperty.MULTIVALUED_STRING_TYPE);
        acceptParamName.setHelpText("Registration query param accept names.");
        configProperties.add(acceptParamName);
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };

    private static String[] QUERY_PARAM_BLACKLIST = {
            "execution",
            "session_code",
            "client_id",
            "tab_id",
            "nonce",
            "response_type",
            "response_mode",
            "scope",
            "redirect_uri",
            "state",
            "phoneNumber",
            "phoneNumberVerified"
    };

    @Override
    public String getDisplayType() {
        return "Redirect parameter reader";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
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
        return "Read query parameter add to user attribute";
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
        validationContext.success();
    }

    @Override
    public void success(FormContext context) {




        String redirectUri = context.getAuthenticationSession().getRedirectUri();
        logger.info("add user attribute form redirectUri:" + redirectUri);
        if (Validation.isBlank(redirectUri)){
            logger.error("no referer. cant get param in keycloak version");
            return;
        }

        HttpUrl url = HttpUrl.parse(redirectUri);
        if (url != null) {
            UserModel user = context.getUser();
            String[] paramNames = null;
            AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
            if (authenticatorConfig != null && authenticatorConfig.getConfig() != null) {
                paramNames = Optional.ofNullable(context.getAuthenticatorConfig().getConfig().get(PARAM_NAMES)).orElse("").split("##");
            }
            String[] finalParamNames = paramNames;
            logger.info("allow query param names:" + finalParamNames);
            url.queryParameterNames()
                    .stream()
                    .filter(v -> (finalParamNames != null && finalParamNames.length > 0) ? Arrays.asList(finalParamNames).contains(v) : !Validation.isBlank(v) && v.length() < 32 && Arrays.stream(QUERY_PARAM_BLACKLIST).noneMatch(item -> item.equals(v)) )

                    .forEach(v -> user.setAttribute(v, url.queryParameterValues(v)));

        }
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
