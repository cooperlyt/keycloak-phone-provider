package cc.coopersoft.keycloak.phone;

import cc.coopersoft.common.OptionalUtils;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.spi.PhoneProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);

    public static Optional<UserModel> findUserByPhone(KeycloakSession session,RealmModel realm, String phoneNumber){

        var userProvider = session.users();
        Set<String> numbers = new HashSet<>();
        numbers.add(phoneNumber);

        if (session.getProvider(PhoneProvider.class).compatibleMode()){
            var phoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                var parsedNumber = phoneNumberUtil.parse(phoneNumber, defaultRegion(session));
                if (parsedNumber.hasNationalNumber()){
                    numbers.add(String.valueOf(parsedNumber.getNationalNumber())) ;
                }
                for (PhoneNumberFormat format : PhoneNumberFormat.values()) {
                    numbers.add(phoneNumberUtil.format(parsedNumber, format));
                }
            }catch (NumberParseException e){
                logger.warn(String.format("%s is not a valid phone number!",phoneNumber),e);
            }
        }


        return numbers.stream().flatMap(number -> userProvider
            .searchForUserByUserAttributeStream(realm,"phoneNumber", number))
            .max((u1, u2) -> {
                var result = comparatorAttributesAnyMatch(u1,u2,"phoneNumberVerified","true"::equals);
                if (result == 0){
                    result = comparatorAttributesAnyMatch(u1,u2,"phoneNumber", number -> number.startsWith("+"));
                }
                return result;
            });

    }

//    public static Optional<UserModel> findUserByPhone(UserProvider userProvider, RealmModel realm, String phoneNumber, String notIs){
//        return userProvider
//            .searchForUserByUserAttributeStream(realm, "phoneNumber", phoneNumber)
//            .filter(u -> !u.getId().equals(notIs))
//            .max(comparatorUser());
//    }

    private static int comparatorAttributesAnyMatch(UserModel user1, UserModel user2,
                                                    String attribute, Predicate<? super String> predicate){
        return Boolean.compare(user1.getAttributeStream(attribute).anyMatch(predicate),
                user2.getAttributeStream(attribute).anyMatch(predicate));
    }

    private static Optional<String> localeToCountry(String locale){
        return OptionalUtils.ofBlank(locale).flatMap(l -> {
            Pattern countryRegx = Pattern.compile("[^a-z]*\\-?([A-Z]{2,3})");
            return Optional.of(countryRegx.matcher(l))
                .flatMap(m -> m.find() ? OptionalUtils.ofBlank(m.group(1)) : Optional.empty());
        });
    }


    private static String defaultRegion(KeycloakSession session){
        var defaultRegion = session.getProvider(PhoneProvider.class).defaultPhoneRegion();
        return defaultRegion.orElseGet(() -> localeToCountry(session.getContext().getRealm().getDefaultLocale()).orElse(null));
    }

    /**
    * Parses a phone number with google's libphonenumber and then outputs it's
    * international canonical form
    *
    */
    public static String canonicalizePhoneNumber(KeycloakSession session,@NotNull String phoneNumber) throws PhoneNumberInvalidException {
        var provider = session.getProvider(PhoneProvider.class);


        var phoneNumberUtil = PhoneNumberUtil.getInstance();
        var resultPhoneNumber = phoneNumber.trim();
        var defaultRegion = defaultRegion(session);
        logger.info(String.format("default region '%s' will be used",defaultRegion));
        try {
            var parsedNumber = phoneNumberUtil.parse(resultPhoneNumber, defaultRegion);
            if (provider.validPhoneNumber() && !phoneNumberUtil.isValidNumber(parsedNumber)) {
                logger.info(String.format("Phone number [%s] Valid fail with google's libphonenumber",resultPhoneNumber));
                throw new PhoneNumberInvalidException(PhoneNumberInvalidException.ErrorType.VALID_FAIL,
                    String.format("Phone number [%s] Valid fail with google's libphonenumber",resultPhoneNumber));
            }

            var canonicalizeFormat = provider.canonicalizePhoneNumber();
            try {
                resultPhoneNumber = canonicalizeFormat
                    .map(PhoneNumberFormat::valueOf)
                    .map(format -> phoneNumberUtil.format(parsedNumber, format))
                    .orElse(resultPhoneNumber);
            }catch (RuntimeException e){
                logger.warn(String.format("canonicalize format param error! '%s' is not in supported list: %s, E164 Will be used.",
                    Arrays.toString(PhoneNumberFormat.values()),
                    canonicalizeFormat.orElse("")),e);
                resultPhoneNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberFormat.E164);
            }

            var phoneNumberRegex = provider.phoneNumberRegex();
            if (!phoneNumberRegex.map(resultPhoneNumber::matches).orElse(true)){
                logger.info(String.format("Phone number [%s] not match regex '%s'",resultPhoneNumber, phoneNumberRegex.orElse("")));
                throw new PhoneNumberInvalidException(PhoneNumberInvalidException.ErrorType.NOT_SUPPORTED,
                    String.format("Phone number [%s] not match regex '%s'",resultPhoneNumber, phoneNumberRegex.orElse("")));
            }
            return resultPhoneNumber;
        }catch (NumberParseException e){
            logger.info(e);
            throw new PhoneNumberInvalidException(e);
        }
    }

    public static boolean isDuplicatePhoneAllowed(KeycloakSession session){
        return session.getProvider(PhoneProvider.class).isDuplicatePhoneAllowed();
    }

    public static int getOtpExpires(KeycloakSession session){
        return session.getProvider(PhoneProvider.class).otpExpires();
    }

}
