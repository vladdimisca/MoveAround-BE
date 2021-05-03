package licenta.mapper.util;

import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;


public class RouteMapperUtil {
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface User {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Car {
    }

    @User
    public UUID user(licenta.model.User user) {
        return user.getId();
    }

    @Car
    public String car(licenta.model.Car car) {
        return car.getLicensePlate();
    }
}
