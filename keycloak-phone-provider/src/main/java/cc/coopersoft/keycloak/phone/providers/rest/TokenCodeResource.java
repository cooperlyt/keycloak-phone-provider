package cc.coopersoft.keycloak.phone.providers.rest;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneProvider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.validation.Validation;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class TokenCodeResource {

  private static final Logger logger = Logger.getLogger(TokenCodeResource.class);
  protected final KeycloakSession session;
  protected final TokenCodeType tokenCodeType;

  TokenCodeResource(KeycloakSession session, TokenCodeType tokenCodeType) {
    this.session = session;
    this.tokenCodeType = tokenCodeType;
  }


  @GET
  @NoCache
  @Path("")
  @Produces(APPLICATION_JSON)
  public Response getTokenCode(@NotBlank @QueryParam("phoneNumber") String phoneNumber,
                               @QueryParam("kind") String kind) {

    if (Validation.isBlank(phoneNumber)) throw new BadRequestException("Must supply a phone number");

    var phoneProvider = session.getProvider(PhoneProvider.class);

    try {
      phoneNumber = Utils.canonicalizePhoneNumber(session,phoneNumber);
    } catch (PhoneNumberInvalidException e) {
      throw new BadRequestException("Phone number is invalid");
    }

    // everybody phones authenticator send AUTH code
    if( !TokenCodeType.REGISTRATION.equals(tokenCodeType) &&
        !TokenCodeType.AUTH.equals(tokenCodeType) &&
        !TokenCodeType.VERIFY.equals(tokenCodeType) &&
        Utils.findUserByPhone(session, session.getContext().getRealm(), phoneNumber).isEmpty()) {
      throw new ForbiddenException("Phone number not found");
    }

    logger.info(String.format("Requested %s code to %s", tokenCodeType.label, phoneNumber));
    int tokenExpiresIn = phoneProvider.sendTokenCode(phoneNumber,
        session.getContext().getConnection().getRemoteAddr(), tokenCodeType, kind);

    String response = String.format("{\"expires_in\":%s}", tokenExpiresIn);

    return Response.ok(response, APPLICATION_JSON_TYPE).build();
  }
}
