package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.jboss.logging.Logger;
import org.keycloak.Config;

import java.util.HashMap;
import java.util.Map;

public class AwsSnsSmsSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(AwsSnsSmsSenderService.class);
    private final Config.Scope config;

    public AwsSnsSmsSenderService(String realmDisplay, Config.Scope config) {
        super(realmDisplay);
        this.config = config;
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();
        String senderId = config.get("sender");

        Map<String, MessageAttributeValue> smsAttributes = null;

        if (senderId != null) {
            smsAttributes = new HashMap<>();
            smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                    .withStringValue(senderId)
                    .withDataType("String"));
        }

        logger.debug("Sending phone verification code via aws sns");

        try {
            sendSMSMessage(snsClient, message, phoneNumber, smsAttributes);
        } catch (Exception ex) {
            String msg = "Could not send message via aws sns";
            logger.error(msg, ex);
            throw new MessageSendException(msg, ex);
        }
    }

    private static void sendSMSMessage(AmazonSNS snsClient, String message,
                                       String phoneNumber, Map<String, MessageAttributeValue> smsAttributes) {
        PublishResult result = snsClient.publish(new PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes));

        logger.debug(String.format("Sent phone verification code via aws sns with message id %s", result.getMessageId()));
    }

    @Override
    public void close() {
    }
}
