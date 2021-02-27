package licenta.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import licenta.model.User;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SmsService {

    SmsService() {
        Twilio.init("", "");
    }

    public void sendConfirmationSms(User user, String activationCode) {

        Message.creator(
                new PhoneNumber(user.getCallingCode() + user.getPhoneNumber()),
                new PhoneNumber(""),
                "" + activationCode)
                .create();
    }
}
