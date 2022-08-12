package cc.coopersoft.keycloak.phone.authentication.authenticators.browser;

import cc.coopersoft.common.OptionalStringUtils;
import cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages;
import cc.coopersoft.keycloak.phone.authentication.requiredactions.ConfigSmsOtpRequiredAction;
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

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;

import static cc.coopersoft.keycloak.phone.authentication.authenticators.browser.PhoneUsernamePasswordForm.VERIFIED_PHONE_NUMBER;
import static cc.coopersoft.keycloak.phone.authentication.authenticators.browser.SmsOtpMfaAuthenticatorFactory.COOKIE_MAX_AGE;
import static cc.coopersoft.keycloak.phone.authentication.forms.SupportPhonePages.FIELD_PHONE_NUMBER;

public class SmsOtpMfaAuthenticator implements Authenticator, CredentialValidator<PhoneOtpCredentialProvider> {

    private static final Logger logger = Logger.getLogger(SmsOtpMfaAuthenticator.class);

    private static final String PAGE = "login-sms-otp.ftl";

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

        if (config != null) maxCookieAge = Integer.parseInt(config.getConfig().get(COOKIE_MAX_AGE));

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

        String phoneNumber = context.getUser().getFirstAttribute(FIELD_PHONE_NUMBER);
        boolean verified = OptionalStringUtils.ofBlank(context.getAuthenticationSession().getAuthNote(VERIFIED_PHONE_NUMBER))
            .map(number -> number.equalsIgnoreCase(phoneNumber))
            .orElse(false);
        if (verified){
            context.success();
            return;
        }

        PhoneProvider phoneProvider = context.getSession().getProvider(PhoneProvider.class);
        Response challenge;
        try {
            int expires = phoneProvider.sendTokenCode(phoneNumber, TokenCodeType.OTP,null);
            challenge = context.form()
                .setInfo("codeSent",phoneNumber)
                .setAttribute("expires",expires)
                .createForm(PAGE);
        } catch (ForbiddenException e) {
            logger.warn("otp send code Forbidden Exception!",e);
            challenge = context.form().setError(SupportPhonePages.Errors.ABUSED.message())
                .createForm(PAGE);
        } catch (Exception e){
            logger.warn("otp send code Exception!",e);
            challenge = context.form().setError(SupportPhonePages.Errors.FAIL.message())
                .createForm(PAGE);
        }
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        boolean validated = validateAnswer(context);
        if (!validated) {
            Response challenge = context.form()
                    .setError(SupportPhonePages.Errors.NOT_MATCH.message())
                    .createForm(PAGE);
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
