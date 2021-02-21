package licenta.service;

import java.security.SecureRandom;

public final class SecureRandomService {

    private static final int PASSWORD_LENGTH = 8;
    private static final int CODE_BOUND = 10000;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }

    public static String generateRandomCode() {
        return String.format("%04d", random.nextInt(CODE_BOUND));
    }
}
