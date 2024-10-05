package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import com.whatsapp.api.WhatsappApiFactory;
import com.whatsapp.api.configuration.ApiVersion;
import com.whatsapp.api.domain.messages.*;
import com.whatsapp.api.domain.messages.type.ButtonSubType;
import com.whatsapp.api.domain.templates.type.LanguageType;
import com.whatsapp.api.impl.WhatsappBusinessCloudApi;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;

public class WhatsappSmsSenderServiceProvider extends FullSmsSenderAbstractService {
    private static final Logger logger = Logger.getLogger(WhatsappSmsSenderServiceProvider.class);
    private final WhatsappBusinessCloudApi client;
    private final String phoneNumberId;
    private final String templateName;
    private final String templateLanguage;

    WhatsappSmsSenderServiceProvider(KeycloakSession session, Scope config){
        super(session);
        var factory = WhatsappApiFactory.newInstance(config.get("access-token"));
        client = factory.newBusinessCloudApi(ApiVersion.V20_0);

        phoneNumberId = config.get("phone-number-id");
        templateName = config.get("template-name");
        templateLanguage = config.get("template-language");
    }

    @Override
    public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires, String kind) throws MessageSendException {
        sendMessage(phoneNumber, code);
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        logger.info("Send otp to " + phoneNumber + ", with message: " + message);

        var templateMessage = new TemplateMessage()
                .setName(templateName)
                .setLanguage(new Language(LanguageType.valueOf(templateLanguage)))
                .addComponent(
                        new BodyComponent()
                                .addParameter(new TextParameter(message)))
                .addComponent(
                        new ButtonComponent()
                                .setSubType(ButtonSubType.URL)
                                .setIndex(0)
                                .addParameter(new TextParameter(message))
                );

        var bodyMessage = Message.MessageBuilder.builder()
                .setTo(phoneNumber)
                .buildTemplateMessage(templateMessage);

        try {
            var response = client.sendMessage(phoneNumberId, bodyMessage);
            for (int i = 0; i < response.messages().size(); i++) {
                var responseMessage = response.messages().get(i);
                logger.info("message " + responseMessage.id() + ": " + responseMessage.messageStatus());
            }
        } catch (Exception e){
            logger.error(e.getMessage());
            throw new MessageSendException(400,
                    String.valueOf(400),
                    e.getMessage());
        }
    }

    @Override
    public void close() {
    }
}
