package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.model.SmsResponseModel;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DummySmsSenderService extends FullSmsSenderAbstractService {
    private final static CloseableHttpClient httpclient = HttpClients.createDefault();
    private final static HttpPost httpPost = new HttpPost("https://rest.payamak-panel.com/api/SendSMS/SendSMS");
    private static final ObjectMapper mapper = new ObjectMapper();
    private final static Logger LOGGER = LoggerFactory.getLogger(DummySmsSenderService.class);

    public DummySmsSenderService(String realmDisplay) {
        super(realmDisplay);
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException, IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", ""));
        urlParameters.add(new BasicNameValuePair("password", ""));
        urlParameters.add(new BasicNameValuePair("from", ""));
        urlParameters.add(new BasicNameValuePair("to", phoneNumber));
        urlParameters.add(new BasicNameValuePair("text", message));
        httpPost.addHeader("content-type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8));

        ResponseHandler<Optional<String>> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (Response.Status.fromStatusCode(status) == Response.Status.OK) {
                HttpEntity responseEntity = response.getEntity();
                return responseEntity != null ? Optional.of(EntityUtils.toString(responseEntity)) : Optional.empty();
            } else {
                LOGGER.error("Error in send share {} {}", status, EntityUtils.toString(response.getEntity()));
                return Optional.empty();
            }
        };

        Optional<String> result = httpclient.execute(httpPost, responseHandler);
        result.ifPresent(LOGGER::info);
        if (result.isPresent()) {
            SmsResponseModel smsResponseModel = mapper.readValue(result.get(), SmsResponseModel.class);
            if (smsResponseModel.getStatus() == 1) {
                if (smsResponseModel.getValue().equals("11")) {
                    throw new MessageSendException(500, "MSG0042", "Insufficient credits to send message");

                }
            } else {
                throw new MessageSendException(500, "MSG0042", "Insufficient credits to send message");
            }
        } else {
            throw new MessageSendException(500, "MSG0042", "Insufficient credits to send message");
        }
        // here you call the method for sending messages
        LOGGER.info(String.format("To: %s >>> %s", phoneNumber, message));
    }

    @Override
    public void close() {
    }
}
