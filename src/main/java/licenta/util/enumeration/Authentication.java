package licenta.util.enumeration;

public enum Authentication {

    HEADER_NAME("Authorization"),
    TOKEN_PREFIX("Bearer "),
    ID_CLAIM("id");

    private final String value;

    Authentication(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
