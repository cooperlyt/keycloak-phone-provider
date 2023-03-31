package cc.coopersoft.keycloak.phone.providers.exception;

import com.google.i18n.phonenumbers.NumberParseException;

public class PhoneNumberInvalidException extends Exception{

  public enum ErrorType {

    NOT_SUPPORTED("invalidPhoneNumberNotSupported"),
    VALID_FAIL("invalidPhoneNumber"),
    INVALID_COUNTRY_CODE("invalidPhoneNumberCountryCode"),
    NOT_A_NUMBER("invalidPhoneNumberMustNumber"),
    TOO_SHORT_AFTER_IDD("invalidPhoneNumberTooShort"),
    TOO_SHORT_NSN("invalidPhoneNumberTooShort"),
    TOO_LONG("invalidPhoneNumberTooLong");

    private final String errorMessage;

    public String message(){
      return errorMessage;
    }

    ErrorType(String message) {
      this.errorMessage = message;
    }
  }

  private final ErrorType errorType;

  public PhoneNumberInvalidException(NumberParseException parseException) {
    super(parseException);
    this.errorType =ErrorType.valueOf(parseException.getErrorType().name());
  }

  public PhoneNumberInvalidException(ErrorType errorType,String message) {
    super(message);
    this.errorType = errorType;
  }

  public ErrorType getErrorType() {
    return errorType;
  }
}
