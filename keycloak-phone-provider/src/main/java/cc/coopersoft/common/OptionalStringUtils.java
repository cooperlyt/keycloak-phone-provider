package cc.coopersoft.common;

import org.keycloak.services.validation.Validation;

import java.util.Optional;

public class OptionalStringUtils {

  public static Optional<String> ofEmpty(String str){
    return Validation.isEmpty(str) ? Optional.empty() : Optional.of(str);
  }

  public static Optional<String> ofBlank(String str){
    return Validation.isBlank(str) ? Optional.empty() : Optional.of(str).map(String::trim);
  }

}
