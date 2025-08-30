package intregrated.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String jwtToken) {
        String subject = "Verify your email address - ITBM-SHOP";
        String verificationUrl = "http://intproj24.sit.kmutt.ac.th/sy4/verify-email/?token=" + jwtToken;

        // HTML content with styled button
        String htmlContent = """
                <html>
                    <body style="font-family: Arial, sans-serif; background-color:#f4f4f4; padding:20px;">
                        <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:8px; box-shadow:0 2px 5px rgba(0,0,0,0.1);">
                            <h2 style="color:#2E86C1; text-align:center;">ITBM-SHOP</h2>
                            <p>Hello,</p>
                            <p>Please verify your email by clicking the button below:</p>
                            <div style="text-align:center; margin:20px 0;">
                                <a href="%s" style="background-color:#28a745; color:white; padding:12px 24px; text-decoration:none; font-weight:bold; border-radius:5px; display:inline-block;">
                                    Verify Email
                                </a>
                            </div>
                            <p>This link will expire in <b>1 hour</b>.</p>
                            <p>Thank you!<br/>- The ITBM-SHOP Team</p>
                        </div>
                    </body>
                </html>
                """.formatted(verificationUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = send as HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
