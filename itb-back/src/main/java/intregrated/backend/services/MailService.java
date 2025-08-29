package intregrated.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your email address";
        String verificationUrl = "http://intproj24.sit.kmutt.ac.th/sy4/verify-email/?token=" + token;

        String message = "Hello,\n\n"
                + "Please verify your email by clicking the link below:\n"
                + verificationUrl + "\n\n"
                + "This link will expire in 1 hour.\n\n"
                + "Thank you!";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }
}
