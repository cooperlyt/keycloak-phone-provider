package cc.coopersoft.keycloak.phone.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.dto.OTPSecretData;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public class PhoneOtpCredentialModel extends CredentialModel {

    public static final String TYPE = "phone-otp";
    private final SmsOtpCredentialData credentialData;
    private final OTPSecretData secretData;

    public PhoneOtpCredentialModel(SmsOtpCredentialData credentialData, OTPSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    private static Optional<CredentialModel> getOtpCredentialModel(@NotNull UserModel user){
        return user.credentialManager()
            .getStoredCredentialsByTypeStream(PhoneOtpCredentialModel.TYPE).findFirst();
    }

    public static Optional<PhoneOtpCredentialModel.SmsOtpCredentialData> getSmsOtpCredentialData(@NotNull UserModel user){
        return getOtpCredentialModel(user)
            .map(credentialModel -> {
                try {
                    return JsonSerialization.readValue(credentialModel.getCredentialData(), PhoneOtpCredentialModel.SmsOtpCredentialData.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            });
    }

    public static void updateOtpCredential(@NotNull UserModel user,
                                           @NotNull PhoneOtpCredentialModel.SmsOtpCredentialData credentialData,
                                           String secretValue){
        getOtpCredentialModel(user)
            .ifPresent(credential -> {
                try {
                    credential.setCredentialData(JsonSerialization.writeValueAsString(credentialData));
                    credential.setSecretData(JsonSerialization.writeValueAsString(new OTPSecretData(secretValue)));
                    PhoneOtpCredentialModel credentialModel = PhoneOtpCredentialModel.createFromCredentialModel(credential);
                    user.credentialManager().updateStoredCredential(credentialModel);
                }catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            });
    }

    public static PhoneOtpCredentialModel create(String phoneNumber, String secretValue,int expires) {
        SmsOtpCredentialData credentialData = new SmsOtpCredentialData(phoneNumber, expires);
        OTPSecretData secretData = new OTPSecretData(secretValue);
        PhoneOtpCredentialModel credentialModel = new PhoneOtpCredentialModel(credentialData, secretData);
        credentialModel.fillCredentialModelFields();
        return credentialModel;
    }

    public static PhoneOtpCredentialModel createFromCredentialModel(CredentialModel credentialModel) {

        try {
            SmsOtpCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), SmsOtpCredentialData.class);
            OTPSecretData secretData = JsonSerialization.readValue(credentialModel.getSecretData(), OTPSecretData.class);
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
            setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            setSecretData(JsonSerialization.writeValueAsString(secretData));
            setType(TYPE);
            setCreatedDate(Time.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SmsOtpCredentialData getOTPCredentialData() {
        return credentialData;
    }

    public OTPSecretData getOTPSecretData() {
        return secretData;
    }



    @Getter
    public static class SmsOtpCredentialData {
        private final String phoneNumber;

        private final long secretCreate;

        private final int expires;

        @JsonIgnore
        public boolean isSecretInvalid(){
            if (expires <= 0){
                return true;
            }
            return  new Date().getTime() > expires * 1000L + secretCreate;
        }

        @JsonCreator
//        @ConstructorProperties("phoneNumber")
        public SmsOtpCredentialData(@JsonProperty("phoneNumber")String phoneNumber,
                                    @JsonProperty("expires") int expires) {
            this.phoneNumber = phoneNumber;
            this.secretCreate = new Date().getTime();
            this.expires = expires;
        }
    }

    public static class EmptySecretData {
    }
}
