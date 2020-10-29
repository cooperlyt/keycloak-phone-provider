package cc.coopersoft.keycloak.providers.sms.constants;

public enum TokenCodeType {
    VERIFY("verification"),
    OTP("authentication"),
    RESET("reset credential"),
    REGISTRATION("registration");

    private String label;

    public String getLabel() {
        return label;
    }

    TokenCodeType(String label) {
        this.label  = label;
    }
}
