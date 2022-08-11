package cc.coopersoft.common;

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

public class OptionalStringUtils {

  public static Optional<String> ofEmpty(String str){
    return StringUtils.isEmpty(str) ? Optional.empty() : Optional.of(str);
  }

  public static Optional<String> ofBlank(String str){
    return StringUtils.isBlank(str) ? Optional.empty() : Optional.of(str).map(String::trim);
  }

}
