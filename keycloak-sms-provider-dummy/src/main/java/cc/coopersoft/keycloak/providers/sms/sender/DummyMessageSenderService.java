package cc.coopersoft.keycloak.providers.sms.sender;

import cc.coopersoft.keycloak.providers.sms.exception.MessageSendException;
import cc.coopersoft.keycloak.providers.sms.spi.FullMessageSenderAbstractService;
import org.jboss.logging.Logger;

import java.util.Random;

public class DummyMessageSenderService extends FullMessageSenderAbstractService {

    private static final Logger logger = Logger.getLogger(DummyMessageSenderService.class);

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {

        // here you call the method for sending messages
        logger.info(String.format("To: %s >>> %s", phoneNumber, message));

        // simulate a failure
        if (new Random().nextInt(10) % 5 == 0) {
            throw new MessageSendException(500, "MSG0042", "Insufficient credits to send message");
        }
    }

    @Override
    public void close() {
    }
}
