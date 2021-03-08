package licenta.service;

import io.smallrye.jwt.build.Jwt;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.InternalServerErrorException;
import licenta.util.Util;
import licenta.util.enumeration.Authentication;
import licenta.util.enumeration.Environment;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class JwtService {

    public String generateJwt(String email, UUID userId) throws InternalServerErrorException {
        return Jwt
                .issuer(getIssuer())
                .upn(email)
                .claim(Authentication.ID_CLAIM.getValue(), userId)
                .sign(getPrivateKey());
    }

    private String getIssuer() throws InternalServerErrorException {
        return Util.getValueOfConfigVariable(Environment.JWT_ISSUER);
    }

    private PrivateKey getPrivateKey() throws InternalServerErrorException {
        String plainPrivateKey = Util.getValueOfConfigVariable(Environment.JWT_PRIVATE_KEY);
        try {
            plainPrivateKey = plainPrivateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+","");

            byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(plainPrivateKey);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
