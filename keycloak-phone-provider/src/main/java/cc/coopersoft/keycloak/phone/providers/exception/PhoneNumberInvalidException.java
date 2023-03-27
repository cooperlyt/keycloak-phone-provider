package cc.coopersoft.keycloak.phone.providers.exception;

import com.google.i18n.phonenumbers.NumberParseException;

public class PhoneNumberInvalidException extends Exception{

  public enum ErrorType {
    VALID_FAIL("invalidPhoneNumber"),
    INVALID_COUNTRY_CODE("invalidPhoneNumberCountryCode"),
    NOT_A_NUMBER("invalidPhoneNumber"),
    TOO_SHORT_AFTER_IDD("invalidPhoneNumber"),
    TOO_SHORT_NSN("invalidPhoneNumber"),
    TOO_LONG("invalidPhoneNumber");

    private final String errorMessage;

    public String message(){
      return errorMessage;
    }

    ErrorType(String message) {
      this.errorMessage = message;
    }
  }
  public PhoneNumberInvalidException(NumberParseException parseException) {
    this.errorType =ErrorType.valueOf(parseException.getErrorType().name());
  }

  public PhoneNumberInvalidException(ErrorType errorType) {
    this.errorType = errorType;
  }

  private ErrorType errorType;

  public ErrorType getErrorType() {
    return errorType;
  }
}
