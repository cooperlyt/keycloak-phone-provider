package cc.coopersoft.keycloak.phone.authentication.forms;

import com.openshift.internal.restclient.URLBuilder;
import okhttp3.HttpUrl;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.touri.URIResolver;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.common.util.UriUtils;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RegistrationRedirectParametersReader implements  FormActionFactory, FormAction {

    private static final Logger logger = Logger.getLogger(RegistrationRedirectParametersReader.class);


    public static final String PROVIDER_ID = "registration-query-parameter";

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
        return "Query parameter reader";
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
        return "Read query parameter add to user attribute";
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
        validationContext.success();
    }

    @Override
    public void success(FormContext context) {

        HttpUrl url = HttpUrl.parse(context.getHttpRequest().getMutableHeaders().getFirst("Referer"));
        if (url != null) {
            UserModel user = context.getUser();

            url.queryParameterNames()
                    .stream()
                    .filter(v -> !Validation.isBlank(v) && v.length() < 32 && Arrays.stream(QUERY_PARAM_BLACKLIST).noneMatch(item -> item.equals(v)))
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
