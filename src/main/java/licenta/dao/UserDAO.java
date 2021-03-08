package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.User;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserDAO implements PanacheRepository<User> {

    public Optional<User> getUserById(UUID userId) {
        return find("id", userId).firstResultOptional();
    }

    public Optional<User> getUserByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> getUserByFullPhoneNumber(String phoneNumber, String callingCode) {
        return find("phone_number = ?1 and calling_code = ?2", phoneNumber, callingCode).firstResultOptional();
    }

    public void updateProfilePictureURLById(UUID userId, String profilePictureURL) {
        update("profile_picture_url = ?1 where id = ?2", profilePictureURL, userId);
    }

    public void updatePasswordById(UUID userId, String password) {
        update("password = ?1 where id = ?2", password, userId);
    }
}
