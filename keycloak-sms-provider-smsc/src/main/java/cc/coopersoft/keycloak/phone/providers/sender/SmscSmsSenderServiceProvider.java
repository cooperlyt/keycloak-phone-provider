package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.keycloak.Config.Scope;

import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmscSmsSenderServiceProvider extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(SmscSmsSenderServiceProvider.class);

    private final String url;
    private final String username;
    private final String password;
    private final Boolean extralog;

    public SmscSmsSenderServiceProvider(Scope config, String realmDisplay) {
        super(realmDisplay);

        this.url = config.get("url");
        this.username = config.get("username");
        this.password = config.get("password");
        this.extralog = Objects.equals(config.get("extralog"), "true");
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        logger.debug("Sending phone verification code via smsc");

        try {
            logger.info(
                    String.format(
                            "Send via smsc. Url: %s; Username: %s; Phone: %s; Message: %s",
                            url, username, phoneNumber, message)
            );
            sendSmsMessage(phoneNumber, message, url, username, password, extralog);
        } catch (Exception ex) {
            String msg = "Could not send message via smsc";
            logger.error(msg, ex);
            throw new MessageSendException(msg, ex);
        }
    }

    private void sendSmsMessage(
            String phoneNumber,
            String message,
            String url,
            String username,
            String password,
            Boolean extralog
    ) throws IOException {
        final MediaType JSON = MediaType.get("application/json");
        final OkHttpClient client = new OkHttpClient();

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("login", username);
        requestBodyJson.put("psw", password);
        requestBodyJson.put("phones", phoneNumber);
        requestBodyJson.put("mes", message);

        if (extralog) {
            logger.info("Request to service: \n" + requestBodyJson.toString(2));
        }

        RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if (extralog) {
                logger.info(response.body().string());
            }

            logger.info("Messages successfully sent.");
        }
    }

    @Override
    public void close() {
    }
}
