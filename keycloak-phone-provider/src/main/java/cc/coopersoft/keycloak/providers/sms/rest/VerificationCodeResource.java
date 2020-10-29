package cc.coopersoft.keycloak.providers.sms.rest;

import cc.coopersoft.keycloak.providers.sms.spi.PhoneMessageService;
import cc.coopersoft.keycloak.providers.sms.spi.TokenCodeService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class VerificationCodeResource {

    private static final Logger logger = Logger.getLogger(VerificationCodeResource.class);
    private final KeycloakSession session;
    private final AuthResult auth;

    VerificationCodeResource(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager().authenticateBearerToken(session, session.getContext().getRealm());
    }

    private TokenCodeService getTokenCodeService() {
        return session.getProvider(TokenCodeService.class);
    }

    @GET
    @NoCache
    @Path("")
    @Produces(APPLICATION_JSON)
    public Response getVerificationCode(@QueryParam("phoneNumber") String phoneNumber) {

        if (phoneNumber == null) throw new BadRequestException("Must inform a phone number");

        logger.info(String.format("Requested verification code to %s", phoneNumber));
        int tokenExpiresIn = session.getProvider(PhoneMessageService.class).sendVerificationCode(phoneNumber);

        String response = String.format("{\"expiresIn\":%s}", tokenExpiresIn);

        return Response.ok(response, APPLICATION_JSON_TYPE).build();
    }

    @POST
    @NoCache
    @Path("")
    @Produces(APPLICATION_JSON)
    public Response checkVerificationCode(@QueryParam("phoneNumber") String phoneNumber,
                                          @QueryParam("code") String code) {

        if (auth == null) throw new NotAuthorizedException("Bearer");
        if (phoneNumber == null) throw new BadRequestException("Must inform a phone number");
        if (code == null) throw new BadRequestException("Must inform a token code");

        UserModel user = auth.getUser();
        getTokenCodeService().validateCode(user, phoneNumber, code);

        return Response.noContent().build();
    }
}
