package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.User;
import licenta.util.enumeration.UserRole;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserDAO implements PanacheRepository<User> {

    public Optional<User> getUserById(UUID userId) {
        return find("id", userId).firstResultOptional();
    }

    public Optional<User> getUserByEmail(String email, UserRole userRole) {
        return find("email = ?1 and role = ?2", email, userRole.getValue()).firstResultOptional();
    }

    public Optional<User> getUserByFullPhoneNumber(String phoneNumber, String callingCode, UserRole userRole) {
        return find("phone_number = ?1 and calling_code = ?2 and role = ?3", phoneNumber, callingCode,
                userRole.getValue()).firstResultOptional();
    }

    public void updateEmailById(UUID userId, String email) {
        update("email = ?1 where id = ?2", email, userId);
    }

    public void updateFirstNameById(UUID userId, String firstName) {
        update("first_name = ?1 where id = ?2", firstName, userId);
    }

    public void updateLastNameById(UUID userId, String lastName) {
        update("last_name = ?1 where id = ?2", lastName, userId);
    }

    public void updateFullPhoneNumberById(UUID userId, String phoneNumber, String callingCode) {
        update("phone_number = ?1, calling_code = ?2 where id = ?3", phoneNumber, callingCode, userId);
    }

    public void updateProfilePictureURLById(UUID userId, String profilePictureURL) {
        update("profile_picture_url = ?1 where id = ?2", profilePictureURL, userId);
    }

    public void updatePasswordById(UUID userId, String password) {
        update("password = ?1 where id = ?2", password, userId);
    }

    public void updateEmailEnabledById(UUID userId, boolean emailEnabled) {
        update("emailEnabled = ?1 where id = ?2", emailEnabled, userId);
    }

    public void updatePhoneEnabledById(UUID userId, boolean phoneEnabled) {
        update("phoneEnabled = ?1 where id = ?2", phoneEnabled, userId);
    }

    public void removeUserById(UUID userId) {
        delete("id", userId);
    }
}
