package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.ActivationCode;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ActivationCodeDAO implements PanacheRepository<ActivationCode> {
    public void updateEmailCodeById(int id, String newActivationCode) {
        update("email_code = ?1 where id = ?2", newActivationCode, id);
    }
}
