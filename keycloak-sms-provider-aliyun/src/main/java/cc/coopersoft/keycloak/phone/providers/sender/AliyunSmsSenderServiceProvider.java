package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderService;
import cc.coopersoft.common.OptionalUtils;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.RealmModel;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AliyunSmsSenderServiceProvider implements MessageSenderService {

  private static final Logger logger = Logger.getLogger(AliyunSmsSenderServiceProvider.class);

  private final Config.Scope config;
  private final RealmModel realm;
  private final AsyncClient client;

  public AliyunSmsSenderServiceProvider(Config.Scope config, RealmModel realm) {
    this.config = config;
    this.realm = realm;

    // HttpClient Configuration
        /*HttpClient httpClient = new ApacheAsyncHttpClientBuilder()
                .connectionTimeout(Duration.ofSeconds(10)) // Set the connection timeout time, the default is 10 seconds
                .responseTimeout(Duration.ofSeconds(10)) // Set the response timeout time, the default is 20 seconds
                .maxConnections(128) // Set the connection pool size
                .maxIdleTimeOut(Duration.ofSeconds(50)) // Set the connection pool timeout, the default is 30 seconds
                // Configure the proxy
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<your-proxy-hostname>", 9001))
                        .setCredentials("<your-proxy-username>", "<your-proxy-password>"))
                // If it is an https connection, you need to configure the certificate, or ignore the certificate(.ignoreSSL(true))
                .x509TrustManagers(new X509TrustManager[]{})
                .keyManagers(new KeyManager[]{})
                .ignoreSSL(false)
                .build();*/


    // Configure Credentials authentication information, including ak, secret, token
    StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
        .accessKeyId(config.get("key"))
        .accessKeySecret(config.get("secret"))
        .securityToken(config.get("token")) // use STS token
        .build());

    // Configure the Client
    client = AsyncClient.builder()
        //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
        .credentialsProvider(provider)
        //.serviceConfiguration(Configuration.create()) // Service-level configuration
        // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
        .overrideConfiguration(
            ClientOverrideConfiguration.create()
                // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
                .setEndpointOverride("dysmsapi.ap-southeast-1.aliyuncs.com")
            //.setConnectTimeout(Duration.ofSeconds(30))
        )
        .build();

  }

  @Override
  public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires, String kind) throws MessageSendException {

    String kindName = OptionalUtils.ofBlank(kind).orElse(type.name().toLowerCase());
    String templateId = Optional.ofNullable(config.get(realm.getName().toLowerCase() + "-" + kindName + "-template"))
        .orElse(config.get(kindName + "-template"));

    // Parameter settings for API request
    SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
        .phoneNumbers(phoneNumber)
        .signName(realm.getDisplayName().toLowerCase())
        .templateCode(templateId)
        .templateParam(String.format("{\"code\":\"%s\",\"expires\":\"%s\"}",code,expires / 60))
        // Request-level configuration rewrite, can set Http request parameters, etc.
        // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
        .build();

    // Asynchronously get the return value of the API request
    CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
    // Synchronously get the return value of the API request
    //SendSmsResponse resp = response.get();
    //System.out.println(new Gson().toJson(resp));
    // Asynchronous processing of return values
        /*response.thenAccept(resp -> {
            System.out.println(new Gson().toJson(resp));
        }).exceptionally(throwable -> { // Handling exceptions
            System.out.println(throwable.getMessage());
            return null;
        });*/

    // Finally, close the client
    client.close();
  }

  @Override
  public void close() {
    client.close();
  }
}
