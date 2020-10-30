package cc.coopersoft.keycloak.phone.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;

import java.beans.ConstructorProperties;
import java.io.IOException;

@Getter
public class PhoneOtpCredentialModel extends CredentialModel {

    public static final String TYPE = "phone-otp";
    private final SmsOtpCredentialData smsOtpCredentialData;
    private final EmptySecretData emptySecretData;

    public PhoneOtpCredentialModel(SmsOtpCredentialData smsOtpCredentialData, EmptySecretData emptySecretData) {
        this.smsOtpCredentialData = smsOtpCredentialData;
        this.emptySecretData = emptySecretData;
    }

    public static PhoneOtpCredentialModel create(String phoneNumber) {

        SmsOtpCredentialData credentialData = new SmsOtpCredentialData(phoneNumber);
        EmptySecretData secretData = new EmptySecretData();
        PhoneOtpCredentialModel credentialModel = new PhoneOtpCredentialModel(credentialData, secretData);

        credentialModel.fillCredentialModelFields();

        return credentialModel;
    }

    public static PhoneOtpCredentialModel createFromCredentialModel(CredentialModel credentialModel) {

        try {
            SmsOtpCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), SmsOtpCredentialData.class);
            EmptySecretData secretData = JsonSerialization.readValue(credentialModel.getSecretData(), EmptySecretData.class);
            PhoneOtpCredentialModel credential = new PhoneOtpCredentialModel(credentialData, secretData);

            credential.setUserLabel(credentialModel.getUserLabel());
            credential.setCreatedDate(credentialModel.getCreatedDate());
            credential.setType(TYPE);
            credential.setId(credentialModel.getId());
            credential.setSecretData(credentialModel.getSecretData());
            credential.setCredentialData(credentialModel.getCredentialData());

            return credential;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCredentialModelFields() {
        try {
            setCredentialData(JsonSerialization.writeValueAsString(smsOtpCredentialData));
            setSecretData(JsonSerialization.writeValueAsString(emptySecretData));
            setType(TYPE);
            setCreatedDate(Time.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    public static class SmsOtpCredentialData {
        private final String phoneNumber;

        @JsonCreator
        @ConstructorProperties("phoneNumber")
        SmsOtpCredentialData(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class EmptySecretData {
    }
}
