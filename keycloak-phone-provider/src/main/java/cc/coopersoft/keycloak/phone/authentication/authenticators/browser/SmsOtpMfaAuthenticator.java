package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.keycloak.phone.authentication.requiredactions.ConfigSmsOtpRequiredAction;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneMessageService;
import org.jboss.resteasy.spi.HttpResponse;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.*;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;

public class SmsOtpMfaAuthenticator implements Authenticator, CredentialValidator<PhoneOtpCredentialProvider> {

    protected boolean hasCookie(AuthenticationFlowContext context) {
        Cookie cookie = context.getHttpRequest()
                .getHttpHeaders()
                .getCookies()
                .get("SMS_OTP_ANSWERED");
        return cookie != null;
    }

    protected boolean validateAnswer(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst("code");
        String credentialId = formData.getFirst("credentialId");
        if (credentialId == null || credentialId.isEmpty()) {
            credentialId = getCredentialProvider(context.getSession())
                    .getDefaultCredential(context.getSession(), context.getRealm(), context.getUser()).getId();
        }

        UserCredentialModel input = new UserCredentialModel(credentialId, getType(context.getSession()), secret);
        return getCredentialProvider(context.getSession()).isValid(context.getRealm(), context.getUser(), input);
    }

    protected void setCookie(AuthenticationFlowContext context) {

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60; // 1 hour

        if (config != null) maxCookieAge = Integer.parseInt(config.getConfig().get("cookie.max.age"));

        URI uri = context.getUriInfo()
                .getBaseUriBuilder()
                .path("realms")
                .path(context.getRealm().getName())
                .build();

        addCookie(context, "SMS_OTP_ANSWERED", "true",
                uri.getRawPath(),
                null, null,
                maxCookieAge,
                false, true);
    }

    public void addCookie(AuthenticationFlowContext context, String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
        HttpResponse response = context.getSession().getContext().getContextObject(HttpResponse.class);
        StringBuffer cookieBuf = new StringBuffer();
        ServerCookie.appendCookieValue(cookieBuf, 1, name, value, path, domain, comment, maxAge, secure, httpOnly, null);
        String cookie = cookieBuf.toString();
        response.getOutputHeaders().add(HttpHeaders.SET_COOKIE, cookie);
    }

    @Override
    public PhoneOtpCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (PhoneOtpCredentialProvider) session.getProvider(CredentialProvider.class, PhoneOtpCredentialProviderFactory.PROVIDER_ID);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (hasCookie(context)) {
            context.success();
            return;
        }
        PhoneMessageService phoneMessageService = context.getSession().getProvider(PhoneMessageService.class);
        String phoneNumber = context.getUser().getFirstAttribute("phoneNumber");
        Response challenge;
        try {
            phoneMessageService.sendTokenCode(phoneNumber, TokenCodeType.OTP);
            challenge = context.form().createForm("login-sms-otp.ftl");
        } catch (ForbiddenException e) {
            challenge = context.form().setError("abusedMessageService").createForm("login-sms-otp.ftl");
        }
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        boolean validated = validateAnswer(context);
        if (!validated) {
            Response challenge = context.form()
                    .setError("authenticationCodeDoesNotMatch")
                    .createForm("login-sms-otp.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        setCookie(context);
        context.success();
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
