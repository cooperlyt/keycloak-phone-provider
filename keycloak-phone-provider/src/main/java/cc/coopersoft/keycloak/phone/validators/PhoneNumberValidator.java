package cc.coopersoft.keycloak.phone.validators;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.validate.*;

import java.util.Collections;
import java.util.List;

public class PhoneNumberValidator extends AbstractStringValidator implements ConfiguredProvider {
    public static final String ID = "phone-number";
    public static final String MESSAGE_INVALID_PHONE_NUMBER = "Error invalid phone number";

    @Override
    public String getHelpText() {
        return "Validate phone number with configured region.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    protected void doValidate(String value, String inputHint, ValidationContext validationContext, ValidatorConfig validatorConfig) {
        try {
            Utils.canonicalizePhoneNumber(validationContext.getSession(), value);
        } catch (PhoneNumberInvalidException e) {
            validationContext.addError(new ValidationError(ID, inputHint, MESSAGE_INVALID_PHONE_NUMBER, value));
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
