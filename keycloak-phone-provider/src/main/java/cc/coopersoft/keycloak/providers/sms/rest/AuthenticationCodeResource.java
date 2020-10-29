package cc.coopersoft.keycloak.providers.sms.rest;

import cc.coopersoft.keycloak.providers.sms.spi.PhoneMessageService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class AuthenticationCodeResource {

    private static final Logger logger = Logger.getLogger(AuthenticationCodeResource.class);
    private final KeycloakSession session;

    AuthenticationCodeResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @NoCache
    @Path("")
    @Produces(APPLICATION_JSON)
    public Response getAuthenticationCode(@QueryParam("phoneNumber") String phoneNumber) {

        if (phoneNumber == null) throw new BadRequestException("Must inform a phone number");

        logger.info(String.format("Requested authentication code to %s", phoneNumber));
        int tokenExpiresIn = session.getProvider(PhoneMessageService.class).sendAuthenticationCode(phoneNumber);

        String response = String.format("{\"expiresIn\":%s}", tokenExpiresIn);

        return Response.ok(response, APPLICATION_JSON_TYPE).build();
    }
}
