package licenta.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(name = "calling_code", nullable = false)
    private String callingCode;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "profile_picture_url")
    private String profilePictureURL;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "activation_code_id", referencedColumnName = "id")
    private ActivationCode activationCode;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "phone_enabled", nullable = false)
    private boolean phoneEnabled;

    public User() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCallingCode() {
        return callingCode;
    }

    public void setCallingCode(String phonePrefix) {
        this.callingCode = phonePrefix;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ActivationCode getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(ActivationCode activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isPhoneEnabled() {
        return phoneEnabled;
    }

    public void setPhoneEnabled(boolean phoneEnabled) {
        this.phoneEnabled = phoneEnabled;
    }
}
