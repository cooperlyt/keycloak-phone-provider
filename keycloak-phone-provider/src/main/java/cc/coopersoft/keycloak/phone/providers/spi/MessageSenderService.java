package cc.coopersoft.keycloak.phone.providers.spi;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import org.keycloak.provider.Provider;


/**
 * SMS, Voice, APP
 */
public interface MessageSenderService extends Provider {

    //void sendVoiceMessage((TokenCodeType type, String realmName, String realmDisplayName, String phoneNumber, String code , int expires) throws MessageSendException;


    void sendSmsMessage(TokenCodeType type, String phoneNumber, String code , int expires , String kind) throws MessageSendException;
}
