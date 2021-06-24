package licenta.util.enumeration;

public enum Configuration {

    JWT_ISSUER("mp.jwt.verify.issuer"),
    JWT_PRIVATE_KEY("jwt.private-key"),
    BUCKET_NAME("app.default-bucket"),
    PERMISSIONS("storage-permissions"),
    AES_SECRET_KEY("app.aes-secret-key"),
    TWILIO_ACCOUNT_SID("twilio-account-sid"),
    TWILIO_AUTH_TOKEN("twilio-auth-token");

    private final String value;

    Configuration(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
