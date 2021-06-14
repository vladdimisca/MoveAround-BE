package licenta.util.enumeration;

public enum Role {
    USER(Constants.USER),
    ADMIN(Constants.ADMIN);

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static class Constants {
        public static final String USER = "USER";
        public static final String ADMIN = "ADMIN";
    }
}
