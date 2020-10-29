package cc.coopersoft.keycloak;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Phone number strategy is Open, easy to user change phone number
 *
 *
 */


public class UserUtil {

    private static UserModel singleUser(List<UserModel> users){
        if (users.isEmpty()) {
            return null;
        }else if (users.size() > 1){
            return users.stream()
                    .filter(u -> u.getAttribute("phoneNumberVerified")
                            .stream().anyMatch("true"::equals))
                    .findFirst().orElse(null);
        }else
            return users.get(0);
    }

    public static UserModel findUserByPhone(UserProvider userProvider, RealmModel realm, String phoneNumber){
        List<UserModel> users = userProvider.searchForUserByUserAttribute(
                "phoneNumber", phoneNumber, realm);
        return singleUser(users);
    }

    public static UserModel findUserByPhone(UserProvider userProvider, RealmModel realm, String phoneNumber, String notIs){
        List<UserModel> users = userProvider.searchForUserByUserAttribute(
                "phoneNumber", phoneNumber, realm);
        return singleUser(users.stream().filter(u -> !u.getId().equals(notIs)).collect(Collectors.toList()));
    }
}
