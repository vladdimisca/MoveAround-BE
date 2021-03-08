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
              "Confirm your account on MoveAround",
              "Hello " + user.getFirstName() + ",\n\n" +
                      "Thanks for signing up with MoveAround! " +
                      "This is your activation code, available for 3 minutes: " + activationCode +
                      "\n\nHave a nice day,\nMoveAround Team"));
    }
}
