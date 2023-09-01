package cc.coopersoft.keycloak.phone.providers.spi;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;

public abstract class FullSmsSenderAbstractService implements MessageSenderService {
    private static final Logger logger = Logger.getLogger(FullSmsSenderAbstractService.class);

    private final String realmDisplay;

    private final KeycloakSession session;

    @Deprecated
    public FullSmsSenderAbstractService(String realmDisplay) {
        this.realmDisplay = realmDisplay;
        this.session = null;
    }

    public FullSmsSenderAbstractService(KeycloakSession session) {
        this.session = session;
        this.realmDisplay = session.getContext().getRealm().getDisplayName();
    }

    public abstract void sendMessage(String phoneNumber, String message) throws MessageSendException;

    @Override
    public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires, String kind)
            throws MessageSendException {
        final String defaultMessage = String.format("[%s] - " + type.label + " code: %s, expires: %s minute ",
                realmDisplay, code, expires / 60);
        final String MESSAGE = localizeMessage(type, phoneNumber, code, expires).orElse(defaultMessage);
        sendMessage(phoneNumber, MESSAGE);
    }

    /**
     * Localizes sms code message template from login theme.
     * 
     * @param type        the type of code sent
     * @param phoneNumber the user's phone number (if applicable)
     * @param code        the verification code
     * @param expires     code expiration in seconds
     * @return The localized string, else empty.
     */
    private Optional<String> localizeMessage(TokenCodeType type, String phoneNumber, String code, int expires) {
        if (this.session != null) {
            try {
                // Get login theme
                final String loginThemeName = session.getContext().getRealm().getLoginTheme();
                final Theme loginTheme = session.theme().getTheme(loginThemeName, Theme.Type.LOGIN);

                // Try get locale from user associated with phone number (if any)
                final Optional<UserModel> user = Utils.findUserByPhone(session, session.getContext().getRealm(),
                        phoneNumber);
                final Optional<String> userLocale = user.map(u -> u.getFirstAttribute(UserModel.LOCALE));

                // Use locale from user or default to realm locale
                final String localeName = userLocale.isPresent() ? userLocale.get()
                        : session.getContext().getRealm().getDefaultLocale();
                final Locale locale = Locale.forLanguageTag(localeName);

                // Get message template from login theme bundle
                final Properties messages = loginTheme.getMessages(locale);
                final String messageTemplate = messages.getProperty("smsCodeMessage");

                // Return nothing when template can't be found
                if (messageTemplate == null || messageTemplate.isBlank()) {
                    return Optional.empty();
                }

                // Format message
                MessageFormat mf = new MessageFormat(messageTemplate, locale);
                return Optional.of(mf.format(new Object[] { realmDisplay, type.label, code, expires / 60 }));
            } catch (Exception ex) {
                logger.error("Error while trying to localize message", ex);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
