package licenta.util;

import licenta.exception.ExceptionMessage;
import licenta.exception.definition.InternalServerErrorException;
import licenta.util.enumeration.Environment;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.core.Response;


public final class Util {

    private Util() {}

    public static String getValueOfConfigVariable(Environment environment) throws InternalServerErrorException {
        return ConfigProvider.getConfig().getOptionalValue(environment.getValue(), String.class).
                orElseThrow(() -> new InternalServerErrorException(
                        ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR));
    }
}
