package licenta.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "activation_codes")
public class ActivationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "email_code")
    private String emailCode;

    private String email;

    @Column(name = "email_created_at")
    private Date emailCreatedAt;

    @Column(name = "sms_code")
    private String smsCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "sms_created_at")
    private Date smsCreatedAt;

    @OneToOne(mappedBy = "activationCode")
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getEmailCreatedAt() {
        return emailCreatedAt;
    }

    public void setEmailCreatedAt(Date emailCreatedAt) {
        this.emailCreatedAt = emailCreatedAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getSmsCreatedAt() {
        return smsCreatedAt;
    }

    public void setSmsCreatedAt(Date smsCreatedAt) {
        this.smsCreatedAt = smsCreatedAt;
    }
}
