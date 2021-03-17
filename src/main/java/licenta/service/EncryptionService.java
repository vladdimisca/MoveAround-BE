package licenta.service;

import licenta.exception.ExceptionMessage;
import licenta.exception.definition.InternalServerErrorException;
import licenta.util.Util;
import licenta.util.enumeration.Configuration;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

@ApplicationScoped
public class EncryptionService {
    private static final WildFlyElytronPasswordProvider PROVIDER = new WildFlyElytronPasswordProvider();

    public String encrypt(String password) throws InternalServerErrorException {
        try {
            PasswordFactory passwordFactory =
                    PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, PROVIDER);
            final int iterationCount = 10;
            final byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];

            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec =
                    new IteratedSaltedPasswordAlgorithmSpec(iterationCount, salt);
            EncryptablePasswordSpec encryptableSpec =
                    new EncryptablePasswordSpec(password.toCharArray(), iteratedAlgorithmSpec);

            BCryptPassword original = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);
            return ModularCrypt.encodeAsString(original);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean passwordMatchesHash(String encodedPassword, String password) throws InternalServerErrorException {

        BCryptPassword originalPassword;
        try {
            PasswordFactory passwordFactory =
                    PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, PROVIDER);

            originalPassword =
                    (BCryptPassword) passwordFactory.translate(ModularCrypt.decode(encodedPassword));
            return passwordFactory.verify(originalPassword, password.toCharArray());
        } catch (InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private SecretKeySpec getSecretKey() throws NoSuchAlgorithmException, InternalServerErrorException {
        // get the secret key from application.properties
        String secretKey = Util.getValueOfConfigVariable(Configuration.AES_SECRET_KEY);

        byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }

    public String encryptAES(String strToEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InternalServerErrorException {

        SecretKeySpec secretKey = getSecretKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptAES(String strToDecrypt) throws NoSuchAlgorithmException, BadPaddingException,
            IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InternalServerErrorException {

        SecretKeySpec secretKey = getSecretKey();
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }
}
