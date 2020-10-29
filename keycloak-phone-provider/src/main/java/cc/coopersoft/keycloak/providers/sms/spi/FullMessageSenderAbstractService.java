package cc.coopersoft.keycloak.providers.sms.spi;

import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import cc.coopersoft.keycloak.providers.sms.exception.MessageSendException;

public abstract class FullMessageSenderAbstractService implements MessageSenderService{

    public abstract void sendMessage(String phoneNumber, String message) throws MessageSendException;

    public void sendMessage(TokenCodeType type, String realmName, String phoneNumber, String code , int expires) throws MessageSendException{
        final String MESSAGE = String.format("%s - " + (TokenCodeType.VERIFY_PHONE_NUMBER.equals(type) ? "" : "登录") + " 验证码: %s , %s 分钟内有效.", realmName, code, String.valueOf(expires / 60));
        sendMessage(phoneNumber,MESSAGE);
    }
}
