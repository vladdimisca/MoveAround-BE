package licenta.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import licenta.model.User;
import licenta.util.enumeration.Configuration;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SmsService {

    public static final String ACCOUNT_SID = System.getenv(Configuration.TWILIO_ACCOUNT_SID.getValue());
    public static final String AUTH_TOKEN = System.getenv(Configuration.TWILIO_AUTH_TOKEN.getValue());

    SmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendConfirmationSms(User user, String activationCode) {

        Message.creator(
                new PhoneNumber("+" + user.getCallingCode() + user.getPhoneNumber()),
                new PhoneNumber("+12562729189"),
                "This is your activation code for Move Around, available within 3 minutes: " + activationCode)
                .create();
    }
}
