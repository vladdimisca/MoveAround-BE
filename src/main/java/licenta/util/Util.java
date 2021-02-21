package licenta.util;

import licenta.exception.ExceptionMessage;
import licenta.exception.definition.InternalServerErrorException;
import licenta.exception.definition.RoleNotFoundException;
import licenta.util.enumeration.Environment;
import licenta.util.enumeration.UserRole;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

public final class Util {

    public static HashSet<String> generateSingleRoleSetFromValue(String role) throws RoleNotFoundException {
        return new HashSet<>(Collections.singletonList(Util.getUserRoleFromValue(role).getValue()));
    }

    public static UserRole getUserRoleFromValue(String role) throws RoleNotFoundException {
        Optional<UserRole> userRoleOptional = UserRole.fromValue(role);
        if (userRoleOptional.isEmpty()) {
            throw new RoleNotFoundException(ExceptionMessage.ROLE_NOT_FOUND, Response.Status.BAD_REQUEST);
        }
        return userRoleOptional.get();
    }

    public static String getValueOfEnvironmentVariable(Environment environment) throws InternalServerErrorException {
        return ConfigProvider.getConfig().getOptionalValue(environment.getValue(), String.class).
                orElseThrow(() -> new InternalServerErrorException(
                        ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR));
    }
}
