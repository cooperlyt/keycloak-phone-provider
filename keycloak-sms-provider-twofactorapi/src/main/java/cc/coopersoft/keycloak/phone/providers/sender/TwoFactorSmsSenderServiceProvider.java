package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;

import javax.annotation.PostConstruct;

public class TwoFactorSmsSenderServiceProvider extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(TwoFactorSmsSenderServiceProvider.class);
    private String twoFactorApiKey;
    private static final String twoFactorUrl = "https://2factor.in/API/V1/";
    private OkHttpClient client;

    @PostConstruct
    public void doSetUp() {
        client = new OkHttpClient().newBuilder()
                .build();
    }

    TwoFactorSmsSenderServiceProvider(Scope config, String realmDisplay) {
        super(realmDisplay);
        this.twoFactorApiKey = config.get("twoFactorApiKey");

    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {

        Request request = new Request.Builder()
                .url(twoFactorUrl + twoFactorApiKey + "/SMS/" + phoneNumber + "/AUTOGEN/OTP1")
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseString = response.body().string();
            if (response.isSuccessful()) {
                logger.info(responseString + ": sms sent successfully");
            } else {
                logger.error(responseString + ": sms sending failed");
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
