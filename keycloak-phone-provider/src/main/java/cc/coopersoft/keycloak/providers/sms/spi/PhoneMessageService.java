package cc.coopersoft.keycloak.providers.sms.spi;

import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import org.keycloak.provider.Provider;

public interface PhoneMessageService extends Provider {


    int sendTokenCode(String phoneNumber, TokenCodeType type);
}
