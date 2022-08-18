package cc.coopersoft.keycloak.phone.providers.rest;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneVerificationCodeProvider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class VerificationCodeResource extends TokenCodeResource {

    private static final Logger logger = Logger.getLogger(VerificationCodeResource.class);

    private final AuthResult auth;

    VerificationCodeResource(KeycloakSession session) {
        super(session, TokenCodeType.VERIFY);
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    private PhoneVerificationCodeProvider getTokenCodeService() {
        return session.getProvider(PhoneVerificationCodeProvider.class);
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
