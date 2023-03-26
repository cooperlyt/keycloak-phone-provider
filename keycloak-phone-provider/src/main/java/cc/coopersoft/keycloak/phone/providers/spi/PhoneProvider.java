package cc.coopersoft.keycloak.phone.providers.spi;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import org.keycloak.provider.Provider;

import java.util.Optional;


public interface PhoneProvider extends Provider {

    //TODO on key login support
    //boolean Verification(String phoneNumber, String token);

    boolean isDuplicatePhoneAllowed(String realm);

    Optional<String> phoneNumberRegx(String realm);

    int sendTokenCode(String phoneNumber, TokenCodeType type, String kind);

    String canonicalizePhoneNumber(String phoneNumber);

    Optional<String> defaultPhoneRegion();

}
