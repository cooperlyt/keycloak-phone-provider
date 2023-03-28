package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.spi.MessageSenderService;
import cc.coopersoft.common.OptionalUtils;
import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.RealmModel;

import java.util.Map;
import java.util.Optional;

public class CloopenSmsSenderServiceProvider implements MessageSenderService {


    private static final String APP_ID_PARAM_NAME = "app";

    private static final String TEMPLATE_PARAM_NAME = "-template";
    private static final Logger logger = Logger.getLogger(CloopenSmsSenderServiceProvider.class);
    private final CCPRestSmsSDK client;

    private final Config.Scope config;
    private final RealmModel realm;

    public CloopenSmsSenderServiceProvider(Config.Scope config, RealmModel realm) {

        this.config = config;
        this.realm = realm;

        //生产环境请求地址：app.cloopen.com
        String serverIp = "app.cloopen.com";
        //请求端口
        String serverPort = "8883";
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = config.get("account");
        String accountToken = config.get("token");


        logger.info(String.format("cloopen account: %s ; accountToken: %s", accountSId, accountToken));

        client = new CCPRestSmsSDK();
        client.init(serverIp, serverPort);
        client.setAccount(accountSId, accountToken);

        client.setBodyType(BodyType.Type_JSON);

    }

    @Override
    public void close() {

    }

    @Override
    public void sendSmsMessage(TokenCodeType type, String phoneNumber, String code, int expires, String kind) throws MessageSendException {
        //请使用管理控制台中已创建应用的APPID
        String appId = Optional.ofNullable(config.get(realm.getName().toLowerCase() + "-" + APP_ID_PARAM_NAME))
                .orElse(config.get(APP_ID_PARAM_NAME));
        client.setAppId(appId);

        String kindName = OptionalUtils.ofBlank(kind).orElse(type.name().toLowerCase());
        String templateId = Optional.ofNullable(config.get(realm.getName().toLowerCase() + "-" + kindName + "-template"))
            .orElse(config.get(kindName + "-template"));

        logger.info(String.format("cloopen appId: %s ; templateId: %s", appId, templateId));

        String[] datas = {code, String.valueOf(expires / 60) };

//        String subAppend="1234";  //可选 扩展码，四位数字 0~9999
//        String reqId="fadfafas";  //可选 第三方自定义消息id，最大支持32位英文数字，同账号下同一自然天内不允许重复
        Map<String, Object> result = client.sendTemplateSMS(phoneNumber,templateId,datas);
        //Map<String, Object> result = client.sendTemplateSMS(phoneNumber,templateId,datas,subAppend,reqId);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            Map<String,Object> data = (Map<String, Object>) result.get("data");
            logger.info("cloopen send message result: " + data.toString());
//            Set<String> keySet = data.keySet();
//            for(String key:keySet){
//                Object object = data.get(key);
//                System.out.println(key +" = "+object);
//
//            }
        }else{
            //异常返回输出错误码和错误信息
            throw new MessageSendException(500, result.get("statusCode").toString(), result.get("statusMsg").toString());
            //System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
    }
}
