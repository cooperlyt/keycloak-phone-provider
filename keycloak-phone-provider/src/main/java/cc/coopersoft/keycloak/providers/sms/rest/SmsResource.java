package cc.coopersoft.keycloak.providers.sms.rest;

import org.keycloak.models.KeycloakSession;

import javax.ws.rs.Path;

public class SmsResource {

    private final KeycloakSession session;

    public SmsResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("verification-code")
    public VerificationCodeResource getVerificationCodeResource() {
        return new VerificationCodeResource(session);
    }

    @Path("authentication-code")
    public AuthenticationCodeResource getAuthenticationCodeResource() {
        return new AuthenticationCodeResource(session);
    }
}
