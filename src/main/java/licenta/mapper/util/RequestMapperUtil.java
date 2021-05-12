package licenta.mapper.util;

import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

public class RequestMapperUtil {
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface User {
    }

    @RequestMapperUtil.User
    public UUID user(licenta.model.User user) {
        return user.getId();
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Route {
    }

    @RequestMapperUtil.Route
    public Integer route(licenta.model.Route route) {
        return route.getId();
    }
}
