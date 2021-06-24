package licenta.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import licenta.model.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    public void sendConfirmationEmail(User user, String activationCode) {
      mailer.send(Mail.withText(user.getEmail(),
              "Confirm your account on Move Around",
              "Hello " + user.getFirstName() + ",\n\n" +
                      "Thanks for signing up with Move Around! " +
                      "This is your activation code, available for 3 minutes: " + activationCode +
                      "\n\nHave a nice day,\nMove Around Team"));
    }

    public void sendNewPassword(User user, String newPassword) {
        mailer.send(Mail.withText(user.getEmail(),
                "Move Around - reset password",
                "Hello " + user.getFirstName() + ",\n\n" +
                        "This is your new password: " + newPassword + "\n" +
                        "For security reasons, we recommend to change it as soon as possible!" +
                        "\n\nHave a nice day,\nMove Around Team"));
    }
}
