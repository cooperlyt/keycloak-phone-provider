package cc.coopersoft.keycloak.providers.sms.spi;

import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import cc.coopersoft.keycloak.providers.sms.exception.MessageSendException;
import org.keycloak.provider.Provider;

public interface MessageSenderService extends Provider {

    //void sendMessage(String phoneNumber, String message) throws MessageSendException;

    void sendMessage(TokenCodeType type,String realmName, String phoneNumber,  String code ,int expires) throws MessageSendException;
}
