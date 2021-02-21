package licenta.util.enumeration;

public enum Environment {

    JWT_ISSUER("mp.jwt.verify.issuer"),
    JWT_PRIVATE_KEY("jwt.private-key"),
    BUCKET_NAME("app.default-bucket"),
    PERMISSIONS("storage-permissions"),
    AES_SECRET_KEY("app.aes-secret-key");

    private final String value;

    Environment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
