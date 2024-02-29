package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import cc.coopersoft.common.OptionalUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;

import java.util.Optional;

public abstract class BaseDirectGrantAuthenticator implements Authenticator {

    public Response errorResponse(int status, String error, String errorDescription) {
        OAuth2ErrorRepresentation errorRep = new OAuth2ErrorRepresentation(error, errorDescription);
        return Response.status(status).entity(errorRep).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    protected Optional<String> getPhoneNumber(AuthenticationFlowContext context){
        return OptionalUtils.ofBlank(OptionalUtils.ofBlank(
            context.getHttpRequest().getDecodedFormParameters().getFirst("phone_number"))
            .orElse(context.getHttpRequest().getDecodedFormParameters().getFirst("phoneNumber")));
    }

    protected Optional<String> getAuthenticationCode(AuthenticationFlowContext context){
        return OptionalUtils.ofBlank(context.getHttpRequest().getDecodedFormParameters().getFirst("code"));
    }

    protected void invalidCredentials(AuthenticationFlowContext context,AuthenticationFlowError error){
        context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
        Response challenge = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
        context.failure(error, challenge);
    }

    protected void invalidCredentials(AuthenticationFlowContext context, UserModel user){
        context.getEvent().user(user);
        invalidCredentials(context,AuthenticationFlowError.INVALID_CREDENTIALS);
    }

    protected void invalidCredentials(AuthenticationFlowContext context){
        invalidCredentials(context,AuthenticationFlowError.INVALID_USER);
    }

    @Override
    public void close() {}

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        authenticate(context);
    }
}
