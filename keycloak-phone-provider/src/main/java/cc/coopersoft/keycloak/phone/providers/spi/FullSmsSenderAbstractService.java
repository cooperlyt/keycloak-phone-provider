package cc.coopersoft.keycloak.phone.providers.spi;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;

public abstract class FullSmsSenderAbstractService implements MessageSenderService{

    public abstract void sendMessage(String phoneNumber, String message) throws MessageSendException;

    public void sendSmsMessage(TokenCodeType type, String realmName, String phoneNumber, String code , int expires) throws MessageSendException{


        final String MESSAGE = String.format("%s - " + type.getLabel() + " code: %s, expires: %s minute ", realmName, code, expires / 60);
        sendMessage(phoneNumber,MESSAGE);
    }
}
