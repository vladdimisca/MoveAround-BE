package licenta.util.enumeration;

import java.util.Optional;

public enum UserRole {

    PASSENGER(Constants.PASSENGER_VALUE),
    DRIVER(Constants.DRIVER_VALUE);

    private final String value;

    UserRole(final String value) {
        this.value = value;
    }

    public static Optional<UserRole> fromValue(String value) {
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }

    public String getValue() {
        return value;
    }

    public static class Constants {
        public static final String PASSENGER_VALUE = "PASSENGER";
        public static final String DRIVER_VALUE = "DRIVER";
    }
}
