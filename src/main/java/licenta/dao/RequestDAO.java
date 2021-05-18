package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.Request;
import licenta.util.enumeration.Status;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RequestDAO implements PanacheRepository<Request> {

    public Optional<Request> getRequestById(Integer requestId) {
        return find("id", requestId).firstResultOptional();
    }

    public List<Request> getRequestsByUserId(UUID userId) {
        return find("user_id = ?1", userId).stream().collect(Collectors.toList());
    }

    public List<Request> getObsoleteRequests() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return find("status = ?1", Status.PENDING)
                .stream()
                .filter(request -> request.getRoute().getStartDate().isBefore(currentDateTime))
                .collect(Collectors.toList());
    }
}
