package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.Config.Scope;

import java.io.IOException;

public class BulksmsSmsSenderServiceProvider extends FullSmsSenderAbstractService {

    public static final String CONFIG_API_SERVER = "url";
    public static final String CONFIG_API_USERNAME = "username";
    public static final String CONFIG_API_PASSWORD = "password";
    public static final String CONFIG_FROM = "from";
    public static final String CONFIG_ENCODING = "encoding";
    public static final String CONFIG_ROUTING_GROUP = "routing-group";

    private static final Logger logger = Logger.getLogger(BulksmsSmsSenderServiceProvider.class);

    private final String url;
    private final String username;
    private final String password;
    private final String from;
    private final String encoding;
    private final String routingGroup;

    private static class BulksmsMessage {
        public String from;
        public String to;
        public String body;
        public String encoding;
        public String routingGroup;

        public BulksmsMessage(String from, String to, String body, String encoding, String routingGroup) {
            this.from = from;
            this.to = to;
            this.body = body;
            this.encoding = encoding;
            this.routingGroup = routingGroup;
        }
    }

    BulksmsSmsSenderServiceProvider(Scope config, KeycloakSession session) {
        super(session);

        String configUrl = config.get(CONFIG_API_SERVER);
        this.url = configUrl != null ? configUrl : "https://api.bulksms.com/v1/messages";
        this.username = config.get(CONFIG_API_USERNAME);
        this.password = config.get(CONFIG_API_PASSWORD);
        this.from = config.get(CONFIG_FROM);
        this.encoding = config.get(CONFIG_ENCODING);
        this.routingGroup = config.get(CONFIG_ROUTING_GROUP);
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        HttpClient httpclient = HttpClients.createDefault();
        SimpleHttp req = SimpleHttp.doPost(url, httpclient);
        req.json(new BulksmsMessage[] {
                new BulksmsMessage(this.from, phoneNumber, message, this.encoding, this.routingGroup) });
        req.authBasic(this.username, this.password);
        try {
            SimpleHttp.Response res = req.asResponse();
            if (res.getStatus() >= 200 || res.getStatus() <= 299) {
                logger.debugv("Sent SMS to {0} with contents: {1}. Server responded with: {2}", phoneNumber, message,
                        res.asString());
            } else {
                logger.errorv("Failed to deliver SMS to {0} with contents: {1}. Server responded with: {2}",
                        phoneNumber,
                        message, res.asString());
                throw new MessageSendException("Bulksms API responded with an error.", new Exception(res.asString()));
            }
        } catch (IOException ex) {
            logger.errorv(ex,
                    "Failed to send SMS to {0} with contents: {1}. An IOException occurred while communicating with SMS service {0}.",
                    phoneNumber, message, url);
            throw new MessageSendException("Error while communicating with Bulksms API.", ex);
        }
    }

    @Override
    public void close() {
    }
}
