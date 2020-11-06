package cc.coopersoft.keycloak.phone.authentication.authenticators.directgrant;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;

public class EverybodyPhoneAuthenticatorFactory extends AuthenticationCodeAuthenticatorFactory{


  public static final String PROVIDER_ID = "everybody-phone-authenticator";

  @Override
  public Authenticator create(KeycloakSession session){
    return new EverybodyPhoneAuthenticator(session);
  }


  @Override
  public String getDisplayType() {
    return "Authentication everybody by phone";
  }

  @Override
  public String getHelpText() {
    return "Authentication everybody by phone";
  }

  @Override
  public String getReferenceCategory() {
    return "Everybody phone Grant";
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
