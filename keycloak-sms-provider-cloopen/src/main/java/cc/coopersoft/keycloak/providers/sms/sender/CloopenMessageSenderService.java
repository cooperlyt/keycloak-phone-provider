package cc.coopersoft.keycloak.providers.sms.sender;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import cc.coopersoft.keycloak.providers.sms.exception.MessageSendException;
import cc.coopersoft.keycloak.providers.sms.spi.MessageSenderService;
import org.jboss.logging.Logger;
import org.keycloak.Config;

import java.util.Map;

public class CloopenMessageSenderService implements MessageSenderService{


    private static final Logger logger = Logger.getLogger(CloopenMessageSenderService.class);
    private final CCPRestSmsSDK client;

    private final Config.Scope config;

    public CloopenMessageSenderService(Config.Scope config) {

        this.config = config;


        //生产环境请求地址：app.cloopen.com
        String serverIp = "app.cloopen.com";
        //请求端口
        String serverPort = "8883";
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = config.get("accountSId");
        String accountToken = config.get("authToken");



        client = new CCPRestSmsSDK();
        client.init(serverIp, serverPort);
        client.setAccount(accountSId, accountToken);

        client.setBodyType(BodyType.Type_JSON);

    }

    @Override
    public void close() {

    }

    @Override
    public void sendMessage(TokenCodeType type, String realmName, String phoneNumber, String code,int expires) throws MessageSendException {
        //请使用管理控制台中已创建应用的APPID
        String appId = config.get(realmName + "_appId");
        client.setAppId(appId);
        String templateId= config.get(realmName + "_" + (TokenCodeType.VERIFY_PHONE_NUMBER.equals(type) ? "v" : "a") + "_templateId");
        String[] datas = {code, String.valueOf(expires / 60) };

//        String subAppend="1234";  //可选 扩展码，四位数字 0~9999
//        String reqId="fadfafas";  //可选 第三方自定义消息id，最大支持32位英文数字，同账号下同一自然天内不允许重复
        Map<String, Object> result = client.sendTemplateSMS(phoneNumber,templateId,datas);
        //Map<String, Object> result = client.sendTemplateSMS(phoneNumber,templateId,datas,subAppend,reqId);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            Map<String,Object> data = (Map<String, Object>) result.get("data");
            logger.debug("cloopen send message result: " + data.toString());
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
