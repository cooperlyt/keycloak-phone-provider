package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderService;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.keycloak.Config;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;

public class AliyunSmsSenderServiceProvider implements MessageSenderService {

  private final Config.Scope config;
  private final RealmModel realm;
  private final IAcsClient client;

  public AliyunSmsSenderServiceProvider(Config.Scope config, RealmModel realm) {
    this.config = config;
    this.realm = realm;
    DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", config.get("accessKeyId"), config.get("accessSecret"));
    client = new DefaultAcsClient(profile);

  }

  @Override
  public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires) throws MessageSendException {

    String templateId= config.get(realm.getName().toUpperCase() + "_" + type.name().toUpperCase() + "_TEMPLATE");
    CommonRequest request = new CommonRequest();
    request.setSysMethod(MethodType.POST);
    request.setSysDomain("dysmsapi.aliyuncs.com");
    request.setSysVersion("2017-05-25");
    request.setSysAction("SendSms");
    request.putQueryParameter("RegionId", "cn-hangzhou");
    request.putQueryParameter("PhoneNumbers", phoneNumber);
    request.putQueryParameter("SignName", realm.getDisplayName());
    request.putQueryParameter("TemplateCode", templateId);

    request.putQueryParameter("TemplateParam", String.format("{\"code\":\"%s\",\"expires\":\"%s\"}",code,expires / 60));
    try {
      CommonResponse response = client.getCommonResponse(request);
      System.out.println(response.getData());
    } catch (ServerException e) {
      e.printStackTrace();
    } catch (ClientException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {

  }
}
