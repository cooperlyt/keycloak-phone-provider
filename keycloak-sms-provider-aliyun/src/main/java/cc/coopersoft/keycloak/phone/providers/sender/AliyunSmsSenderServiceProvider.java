package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderService;
import cc.coopersoft.common.OptionalUtils;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.RealmModel;

import java.util.Optional;

public class AliyunSmsSenderServiceProvider implements MessageSenderService {

  private static final Logger logger = Logger.getLogger(AliyunSmsSenderServiceProvider.class);

  private final Config.Scope config;
  private final RealmModel realm;
  private final IAcsClient client;

  public AliyunSmsSenderServiceProvider(Config.Scope config, RealmModel realm) {
    this.config = config;
    this.realm = realm;
    DefaultProfile profile = DefaultProfile.getProfile(config.get("region") , config.get("key"), config.get("secret"));
    client = new DefaultAcsClient(profile);

  }

  @Override
  public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires, String kind) throws MessageSendException {

    String kindName = OptionalUtils.ofBlank(kind).orElse(type.name().toLowerCase());
    String templateId = Optional.ofNullable(config.get(realm.getName().toLowerCase() + "-" + kindName + "-template"))
        .orElse(config.get(kindName + "-template"));

    CommonRequest request = new CommonRequest();
    request.setSysMethod(MethodType.POST);
    request.setSysDomain("dysmsapi.aliyuncs.com");
    request.setSysVersion("2017-05-25");
    request.setSysAction("SendSms");
    request.putQueryParameter("RegionId", config.get("region"));
    request.putQueryParameter("PhoneNumbers", phoneNumber);
    request.putQueryParameter("SignName", realm.getDisplayName().toLowerCase());
    request.putQueryParameter("TemplateCode", templateId);

    request.putQueryParameter("TemplateParam", String.format("{\"code\":\"%s\",\"expires\":\"%s\"}",code,expires / 60));
    try {
      CommonResponse response = client.getCommonResponse(request);
      logger.debug(response.getData());
    } catch (ClientException e) {
      throw new MessageSendException(500,e.getErrCode(),e.getMessage());
    }
  }

  @Override
  public void close() {

  }
}
