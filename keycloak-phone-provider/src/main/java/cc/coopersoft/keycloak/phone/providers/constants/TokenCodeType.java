package cc.coopersoft.keycloak.phone.providers.constants;

public enum TokenCodeType {
    VERIFY("verification", false),
    AUTH("authentication", false),

    OTP("OTP", true),
    OTP_CONFIGURE("OTP configure", true),
    RESET("reset credential", false),
    REGISTRATION("registration", false);

    public final String label;

    public final boolean isOTP;

    TokenCodeType(String label, boolean isOTP) {
        this.label  = label;
        this.isOTP = isOTP;
    }
}
