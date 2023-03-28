package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderService;
import cc.coopersoft.common.OptionalUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.Config;
import org.keycloak.models.RealmModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class YunxinSmsSenderServiceProvider implements MessageSenderService {

  private final Config.Scope config;
  private final RealmModel realm;

  //发送验证码的请求路径URL
  private static final String
          SERVER_URL="https://api.netease.im/sms/sendcode.action";


//  //验证码长度，范围4～10，默认为4
//  private static final String CODELEN="6";

  public YunxinSmsSenderServiceProvider(Config.Scope config, RealmModel realm) {
    this.config = config;
    this.realm = realm;
  }

  @Override
  public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires, String kind) throws MessageSendException {

    HttpPost httpPost = new HttpPost(SERVER_URL);
    String curTime = String.valueOf((new Date()).getTime() / 1000L);
    /*
     * 参考计算CheckSum的java代码，在上述文档的参数列表中，有CheckSum的计算文档示例
     */
    String checkSum = CheckSumBuilder.getCheckSum(config.get("secret"), code, curTime);

    // 设置请求的的参数，requestBody参数
    List<NameValuePair> nvps = new ArrayList<>();
    /*
     * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档”
     * 2.参数格式是jsonArray的格式，例如 "['13888888888','13666666666']"
     * 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
     */
    String kindName = OptionalUtils.ofBlank(kind).orElse(type.name().toLowerCase());
    String templateId = Optional.ofNullable(config.get(realm.getName().toLowerCase() + "-" + kindName + "-template"))
        .orElse(config.get(kindName + "-template"));
    nvps.add(new BasicNameValuePair("templateid", templateId));
    nvps.add(new BasicNameValuePair("mobile", phoneNumber));
    nvps.add(new BasicNameValuePair("codeLen", String.valueOf(code.length())));


    // 执行请求
    try {
      /*
       * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500
       * 2.具体的code有问题的可以参考官网的Code状态表
       */
      httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
      HttpResponse response = HttpClientBuilder.create().setDefaultHeaders(List.of(
              new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8"),
              new BasicHeader("AppKey",Optional.ofNullable(config.get(realm.getName().toLowerCase() + "-app")).orElse(config.get("app"))),
              new BasicHeader("Nonce",code),
              new BasicHeader("CurTime",curTime),
              new BasicHeader("CheckSum",checkSum)))
              .build().execute(httpPost);
      System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void close() {

  }
}
