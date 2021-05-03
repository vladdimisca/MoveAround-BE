package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.Route;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RouteDAO implements PanacheRepository<Route> {

    public Optional<Route> getRouteById(Integer routeId) {
        return find("id", routeId).firstResultOptional();
    }

    public List<Route> getRoutesByUserId(UUID userId) {
        return find("user_id = ?1", userId).stream().collect(Collectors.toList());
    }
}
