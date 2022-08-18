package cc.coopersoft.keycloak.phone.providers.constants;

public enum TokenCodeType {
    VERIFY("verification"),
    AUTH("authentication"),

    OTP("OTP"),
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
