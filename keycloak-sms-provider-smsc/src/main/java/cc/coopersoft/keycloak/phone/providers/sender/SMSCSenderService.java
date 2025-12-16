package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public class SMSCSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(Smsc.class);

    private final Smsc smsc;

    public SMSCSenderService(KeycloakSession session, Config.Scope config) {
        super(session);
        smsc = new Smsc(config.get("login"), config.get("password"));
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        try {
            String[] response = smsc.sendSms(phoneNumber, message, 1, "", "", 0, "", "");

            if (response.length == 0) {
                throw new MessageSendException(500, "Не получен ответ от сервера", "");
            }

            logger.info(String.format("Сообщение успешно отправлено на номер %s.", phoneNumber));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new MessageSendException(500, "500", e.getMessage());
        }
    }

    @Override
    public void close() {

    }
}
