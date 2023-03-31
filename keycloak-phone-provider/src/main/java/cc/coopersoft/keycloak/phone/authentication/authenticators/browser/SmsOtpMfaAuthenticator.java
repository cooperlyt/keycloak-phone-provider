package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.common.OptionalUtils;
import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.authentication.requiredactions.ConfigSmsOtpRequiredAction;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneProvider;
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpResponse;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.*;
import org.keycloak.models.credential.dto.OTPSecretData;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static cc.coopersoft.keycloak.phone.authentication.authenticators.browser.PhoneUsernamePasswordForm.VERIFIED_PHONE_NUMBER;
import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.ATTRIBUTE_SUPPORT_PHONE;

public class SmsOtpMfaAuthenticator implements Authenticator, CredentialValidator<PhoneOtpCredentialProvider> {

  private static final Logger logger = Logger.getLogger(SmsOtpMfaAuthenticator.class);

  private static final String PAGE = "login-sms-otp.ftl";

  protected boolean validateCookie(AuthenticationFlowContext context) {
    if (Utils.getOtpExpires(context.getSession()) <= 0)
      return false;

    var invalid = PhoneOtpCredentialModel.getSmsOtpCredentialData(context.getUser())
        .map(PhoneOtpCredentialModel.SmsOtpCredentialData::isSecretInvalid)
        .orElse(true);

    if (invalid)
      return false;

    return Optional.of(context.getHttpRequest().getHttpHeaders().getCookies())
            .flatMap(cookies ->
                    Optional.ofNullable(cookies.get("SMS_OTP_ANSWERED"))
                    .flatMap(cookie -> OptionalUtils.ofBlank(cookie.getValue()))
                    .flatMap(credentialId ->
                        Optional.ofNullable(cookies.get(credentialId))
                            .flatMap(cookie -> OptionalUtils.ofBlank(cookie.getValue()))
                            .map(secret ->  context.getUser()
                                .credentialManager()
                                .isValid(new UserCredentialModel(credentialId, getType(context.getSession()), secret)))
                    )
            ).orElse(false);
  }

  protected void setCookie(AuthenticationFlowContext context, String credentialId, String secret) {


    int maxCookieAge = Utils.getOtpExpires(context.getSession());

    if (maxCookieAge <= 0 ){
      return;
    }

    URI uri = context.getUriInfo()
        .getBaseUriBuilder()
        .path("realms")
        .path(context.getRealm().getName())
        .build();

    addCookie(context, "SMS_OTP_ANSWERED", credentialId,
        uri.getRawPath(),
        null, null,
        maxCookieAge,
        false, true);
    addCookie(context, credentialId, secret,
        uri.getRawPath(),
        null, null,
        maxCookieAge,
        false, true);
  }

  public void addCookie(AuthenticationFlowContext context, String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
    HttpResponse response = context.getSession().getContext().getContextObject(HttpResponse.class);
    StringBuilder cookieBuf = new StringBuilder();
    ServerCookie.appendCookieValue(cookieBuf, 1, name, value, path, domain, comment, maxAge, secure, httpOnly, null);
    String cookie = cookieBuf.toString();
    response.getOutputHeaders().add(HttpHeaders.SET_COOKIE, cookie);
  }

  @Override
  public PhoneOtpCredentialProvider getCredentialProvider(KeycloakSession session) {
    return (PhoneOtpCredentialProvider) session.getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);
  }

  private String getCredentialPhoneNumber(UserModel user){
    return PhoneOtpCredentialModel.getSmsOtpCredentialData(user)
        .map(PhoneOtpCredentialModel.SmsOtpCredentialData::getPhoneNumber)
        .orElseThrow(() -> new IllegalStateException("Not have OTP Credential"));
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {

    if (validateCookie(context)) {
      context.success();
      return;
    }

    String phoneNumber = getCredentialPhoneNumber(context.getUser());

    boolean verified = OptionalUtils.ofBlank(context.getAuthenticationSession().getAuthNote(VERIFIED_PHONE_NUMBER))
        .map(number -> number.equalsIgnoreCase(phoneNumber))
        .orElse(false);
    if (verified) {
      context.success();
      return;
    }

    PhoneProvider phoneProvider = context.getSession().getProvider(PhoneProvider.class);
    try {
      int expires = phoneProvider.sendTokenCode(phoneNumber,context.getConnection().getRemoteAddr(),
          TokenCodeType.OTP, null);
      context.form()
          .setInfo("codeSent", phoneNumber)
          .setAttribute("expires", expires)
          .setAttribute("initSend",true);
    } catch (ForbiddenException e) {
      logger.warn("otp send code Forbidden Exception!", e);
      context.form().setError(SupportPhonePages.Errors.ABUSED.message());
    } catch (Exception e) {
      logger.warn("otp send code Exception!", e);
      context.form().setError(SupportPhonePages.Errors.FAIL.message());
    }

    var credentialData = new PhoneOtpCredentialModel.SmsOtpCredentialData(phoneNumber,0);
    PhoneOtpCredentialModel.updateOtpCredential(context.getUser(),credentialData,null);

    Response challenge = challenge(context,phoneNumber);
    context.challenge(challenge);
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    String secret = formData.getFirst("code");
    String credentialId = formData.getFirst("credentialId");

    String phoneNumber = getCredentialPhoneNumber(context.getUser());

    if (credentialId == null || credentialId.isEmpty()) {
      var defaultOtpCredential = getCredentialProvider(context.getSession())
          .getDefaultCredential(context.getSession(), context.getRealm(), context.getUser());
      credentialId = defaultOtpCredential==null ? "" : defaultOtpCredential.getId();
    }

    if (Validation.isBlank(secret)){
      context.form()
          .setError(SupportPhonePages.Errors.NOT_MATCH.message());
      Response challenge = challenge(context,phoneNumber);
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
    }

    UserCredentialModel input = new UserCredentialModel(credentialId, getType(context.getSession()), secret);

    boolean validated = getCredentialProvider(context.getSession()).isValid(context.getRealm(), context.getUser(), input);

    if (!validated) {
      context.form()
          .setError(SupportPhonePages.Errors.NOT_MATCH.message());
      Response challenge = challenge(context,phoneNumber);
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
      return;
    }
    setCookie(context,credentialId,secret);
    context.success();
  }

  protected Response challenge(AuthenticationFlowContext context,String phoneNumber) {
    return context.form()
        .setAttribute(ATTRIBUTE_SUPPORT_PHONE, true)
        .setAttribute(SupportPhonePages.ATTEMPTED_PHONE_NUMBER,phoneNumber)
        .createForm(PAGE);
  }

  @Override
  public boolean requiresUser() {
    return true;
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return getCredentialProvider(session).isConfiguredFor(realm, user, getType(session));
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    user.addRequiredAction(ConfigSmsOtpRequiredAction.PROVIDER_ID);
  }

  @Override
  public void close() {

  }
}
