package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;

public class TwoFactorSmsSenderServiceProvider extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(TwoFactorSmsSenderServiceProvider.class);
    private final String apiKey;
    private static final String twoFactorUrl = "https://2factor.in/API/V1/";
    private final OkHttpClient client;

    TwoFactorSmsSenderServiceProvider(KeycloakSession session, Scope config) {
        super(session);
        apiKey = config.get("key");
        client = new OkHttpClient().newBuilder()
            .build();

    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {

        Request request = new Request.Builder()
                .url(twoFactorUrl + apiKey + "/SMS/" + phoneNumber + "/AUTOGEN/OTP1")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
                logger.info(responseString + ": sms sent successfully");
            } else {
                throw new MessageSendException(response.code(),
                        String.valueOf(response.code()),
                        response.message());
            }
        } catch (Exception e) {
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
