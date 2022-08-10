package cc.coopersoft.keycloak.phone.providers.spi;

import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

public interface TokenCodeService extends Provider {

    TokenCodeRepresentation ongoingProcess(String phoneNumber, TokenCodeType tokenCodeType);

    boolean isAbusing(String phoneNumber, TokenCodeType tokenCodeType,int hourMaximum);

    void persistCode(TokenCodeRepresentation tokenCode, TokenCodeType tokenCodeType, int tokenExpiresIn);

    void validateCode(UserModel user, String phoneNumber, String code);

    void validateCode(UserModel user, String phoneNumber, String code, TokenCodeType tokenCodeType);

    void validateProcess(String tokenCodeId, UserModel user);

    void cleanUpAction(UserModel user);

    void tokenValidated(UserModel user, String phoneNumber, String tokenCodeId);
}
