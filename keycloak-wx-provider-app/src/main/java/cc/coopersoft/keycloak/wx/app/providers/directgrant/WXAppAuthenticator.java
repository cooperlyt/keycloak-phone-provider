package cc.coopersoft.keycloak.wx.app.providers.directgrant;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.utils.StringUtil;

import java.io.IOException;
import java.util.Optional;

public class WXAppAuthenticator implements Authenticator {

  private static final Logger logger = Logger.getLogger(WXAppAuthenticator.class);

  private static final String CODE_PARAM_NAME = "code";

  private static final String WX_CODE_TO_SESSION_URI = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

  private static final String USER_UNION_ID_ATTRIBUTE = "wx_union_id";

  private static final String USER_OPEN_ID_ATTRIBUTE = "wx_open_id";

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    context.clearUser();
    getCode(context).flatMap(code -> getWXUserSession(context,code)).ifPresent(session ->
      validUser(context, session.unionid, session.openid));
  }

  @Override
  public void action(AuthenticationFlowContext authenticationFlowContext) {
    authenticate(authenticationFlowContext);
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
  }

  @Override
  public void close() {
  }

  private Response errorResponse(int status, String error, String errorDescription) {
    OAuth2ErrorRepresentation errorRep = new OAuth2ErrorRepresentation(error, errorDescription);
    return Response.status(status).entity(errorRep).type(MediaType.APPLICATION_JSON_TYPE).build();
  }

  private Optional<String> getCode(AuthenticationFlowContext context) {
    String code = context.getHttpRequest().getDecodedFormParameters().getFirst(CODE_PARAM_NAME);
    if (StringUtil.isNotBlank(code)){
      return Optional.of(code);
    }
    context.getEvent().error(Errors.INVALID_REQUEST);
    Response challenge = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_request",
        "Invalid WX APP grant request, code is must");
    context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
    return Optional.empty();
  }

  private Optional<WXUserSessionInfo> getWXUserSession(AuthenticationFlowContext context, String code){
    logger.info("WX APP grant request by:" + code);

    return getWXAPICredentials(context).flatMap(cer -> {
      OkHttpClient okHttpClient = new OkHttpClient();

      Request request = new Request.Builder()
          .addHeader("content-type", "application/json")
          .url(String.format(WX_CODE_TO_SESSION_URI, cer.id, cer.secret, code))
          .get()
          .build();


      try (okhttp3.Response response = okHttpClient.newCall(request).execute()){
        if (response.isSuccessful() && response.body() != null){
          ObjectMapper objectMapper = new ObjectMapper();
          WXUserSessionInfo result = objectMapper.readValue(response.body().string(), WXUserSessionInfo.class);
          logger.debug("wx api result: " + result);
          if (result.valid()){
            return Optional.of(result);
          }

          logger.error("WX API response invalid " + result.errcode + ":" + result.errmsg);
          context.getEvent().error(Errors.INVALID_REQUEST);
          Response challenge = errorResponse(Response.Status.BAD_GATEWAY.getStatusCode(), "invalid_wx_api_exception",
              "WX API call invalid" + result.errcode + ":" + result.errmsg);
          context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
          return Optional.empty();
        }

        logger.error("WX API call exception, code:" + response.code());
        context.getEvent().error(Errors.INVALID_REQUEST);
        Response challenge = errorResponse(Response.Status.BAD_GATEWAY.getStatusCode(), "invalid_wx_api_exception",
            "WX API call exception, code:" + response.code());
        context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
        return Optional.empty();

      } catch (IOException e) {
        logger.error(e.getMessage(),e);
        context.getEvent().error(Errors.INVALID_REQUEST);
        Response challenge = errorResponse(Response.Status.BAD_GATEWAY.getStatusCode(), "invalid_wx_api_exception",
            "WX API call exception");
        context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
        return Optional.empty();
      }
    });
  }

  private boolean isAllowEvery(AuthenticationFlowContext context) {
    return context.getAuthenticatorConfig() == null ||
        context.getAuthenticatorConfig().getConfig().getOrDefault(WXAppAuthenticatorFactory.CONFIG_EVERY, "true")
            .equals("true");
  }

  private Optional<WXAPICredentials> getWXAPICredentials(AuthenticationFlowContext context){

    String appid = context.getAuthenticatorConfig().getConfig().get(WXAppAuthenticatorFactory.WX_API_ID);
    String secret = context.getAuthenticatorConfig().getConfig().get(WXAppAuthenticatorFactory.WX_API_SECRET);

    if (StringUtil.isNotBlank(appid) && StringUtil.isNotBlank(secret)){
      return Optional.of(new WXAPICredentials(appid,secret));
    }

    context.getEvent().error(Errors.INVALID_REQUEST);
    Response challenge = errorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "invalid_api_credentials",
        "WX API credentials not config");
    context.failure(AuthenticationFlowError.ACCESS_DENIED, challenge);
    return Optional.empty();

  }

  private Optional<UserModel> findUser(AuthenticationFlowContext context, String unionId, String openId){

    var userProvider = context.getSession().users();

    return userProvider.searchForUserByUserAttributeStream(context.getRealm(), USER_UNION_ID_ATTRIBUTE, unionId).findFirst()
        .or(() -> userProvider.searchForUserByUserAttributeStream(context.getRealm(), USER_OPEN_ID_ATTRIBUTE, openId).findFirst())
        .or(() -> createEveryUser(context,unionId,openId));

  }

  private Optional<UserModel> createEveryUser(AuthenticationFlowContext context, String unionId, String openId){
    if (isAllowEvery(context)){
      UserModel newUser = context.getSession().users().addUser(context.getRealm(), unionId);

      newUser.setEnabled(true);
      newUser.setSingleAttribute(USER_UNION_ID_ATTRIBUTE, unionId);
      newUser.setSingleAttribute(USER_OPEN_ID_ATTRIBUTE, openId);
      //context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, unionId);
      logger.info("create user by wx :" + unionId);
      return Optional.of(newUser);

    }
    context.getEvent().error(Errors.USER_NOT_FOUND);
    Response challenge = errorResponse(Response.Status.FORBIDDEN.getStatusCode(), "invalid_user",
        "Invalid WX User:" +  unionId);
    context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
    return Optional.empty();
  }

  private void validUser(AuthenticationFlowContext context, String unionId, String openId){
    findUser(context,unionId,openId).ifPresent(
        user -> {
          context.setUser(user);
          context.success();
        }
    );
  }


  @Data
  @NoArgsConstructor
  private static class WXUserSessionInfo {
    private String sessionKey;	//	会话密钥
    private String unionid;//	用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台账号下会返回，详见 UnionID 机制说明。
    private String errmsg; //	错误信息
    private String openid;//	用户唯一标识
    private int errcode;//	int32	错误码

    public boolean valid(){
      return StringUtil.isNotBlank(unionid) && StringUtil.isNotBlank(openid);
    }

  }

  private record WXAPICredentials(String id, String secret) {

  }
}
