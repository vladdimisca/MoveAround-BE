package licenta.mapper.util;

import licenta.dto.UserDTO;
import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class RouteMapperUtil {
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Car {
    }

    @Car
    public String car(licenta.model.Car car) {
        return car.getLicensePlate();
    }
}
