package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.Route;
import licenta.util.enumeration.TravelRole;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
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
        List<Route> routes = find("user_id = ?1", userId).stream().collect(Collectors.toList());
        Collections.reverse(routes);
        return routes;
    }

    public List<Route> getPossibleRoutes(UUID userId, LocalDateTime dateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return find("user_id != ?1", userId)
                .stream()
                .filter(route ->
                            route.getStartDate().isAfter(currentDateTime) &&
                            route.getStartDate().isAfter(dateTime.minus(Duration.ofHours(4))) &&
                            route.getStartDate().isBefore(dateTime.plus(Duration.ofHours(4))))
                .collect(Collectors.toList());
    }

    public Long getNoOfRoutesAsDriver() {
        return find("parentroute_id IS NULL").stream().count();
    }

    public Long getNoOfRoutesAsPassenger() {
        return find("parentroute_id IS NOT NULL").stream().count();
    }
}
