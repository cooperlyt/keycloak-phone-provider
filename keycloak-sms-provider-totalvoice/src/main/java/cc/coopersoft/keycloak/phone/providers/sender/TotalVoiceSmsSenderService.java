package cc.coopersoft.keycloak.phone.providers.sender;

import br.com.totalvoice.TotalVoiceClient;
import br.com.totalvoice.api.Sms;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import org.json.JSONObject;
import org.keycloak.Config.Scope;

public class TotalVoiceSmsSenderService extends FullSmsSenderAbstractService {

    private final TotalVoiceClient client;
    private final Sms smsClient;

    TotalVoiceSmsSenderService(Scope config) {
        this.client = new TotalVoiceClient(config.get("authToken"));
        this.smsClient = new Sms(client);
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {

        try {
            JSONObject response = smsClient.enviar(phoneNumber, message);

            if (!response.getBoolean("sucesso")) {
                throw new MessageSendException(response.getInt("status"),
                        String.valueOf(response.getInt("motivo")),
                        response.getString("mensagem"));
            }
        } catch (Exception e) {
            throw new MessageSendException(500, "500", "Unexpected exception");
        }
    }

    @Override
    public void close() {
    }
}
