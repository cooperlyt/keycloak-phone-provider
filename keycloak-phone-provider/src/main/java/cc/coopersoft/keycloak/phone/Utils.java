package cc.coopersoft.keycloak.phone;

import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import com.google.i18n.phonenumbers.*;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.Optional;


/**
 *
 *
 *
 */


public class Utils {

    public static Optional<UserModel> findUserByPhone(UserProvider userProvider, RealmModel realm, String phoneNumber){
        return userProvider
            .searchForUserByUserAttributeStream(realm,"phoneNumber", phoneNumber)
            .max(comparatorUser());
    }

    public static Optional<UserModel> findUserByPhone(UserProvider userProvider, RealmModel realm, String phoneNumber, String notIs){
        return userProvider
            .searchForUserByUserAttributeStream(realm, "phoneNumber", phoneNumber)
            .filter(u -> !u.getId().equals(notIs))
            .max(comparatorUser());
    }

    private static Comparator<UserModel> comparatorUser() {
        return (u1, u2) ->
            Boolean.compare(u1.getAttributeStream("phoneNumberVerified").anyMatch("true"::equals),
                u2.getAttributeStream("phoneNumberVerified").anyMatch("true"::equals));
    }

    public static boolean isDuplicatePhoneAllowed(KeycloakSession session){
        return session.getProvider(PhoneProvider.class).isDuplicatePhoneAllowed();
    }

    /**
    * Parses a phone number with google's libphonenumber and then outputs it's
    * international canonical form
    *
    */
    public static String canonicalizePhoneNumber(KeycloakSession session,@NotNull String phoneNumber) throws PhoneNumberInvalidException {
        var provider = session.getProvider(PhoneProvider.class);

        var defaultRegion = provider.defaultPhoneRegion();
        var phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            var parsedNumber = phoneNumberUtil.parse(phoneNumber.trim(), defaultRegion.orElse(null));
            if (provider.validPhoneNumber() && !phoneNumberUtil.isValidNumber(parsedNumber)) {
                throw new PhoneNumberInvalidException(PhoneNumberInvalidException.ErrorType.VALID_FAIL);
            }

            if (provider.canonicalizePhoneNumber()) {
                return phoneNumberUtil.format(parsedNumber, PhoneNumberFormat.INTERNATIONAL);
            }
            return phoneNumber.trim();
        }catch (NumberParseException e){
            throw new PhoneNumberInvalidException(e);
        }
    }


}
