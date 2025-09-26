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
        String verificationUrl = "https://intproj24.sit.kmutt.ac.th/sy4/verify-email/?token=" + jwtToken;

        // HTML content with styled button
        String htmlContent = """
                <html>
                <head>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap');
                    </style>
                </head>
                <body style="font-family: 'Roboto', sans-serif; background-color: #f0f2f5; margin: 0; padding: 40px 0; text-align: center;">
                
                    <div style="max-width: 500px; margin: auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08); overflow: hidden;">
                
                        <div style="background-color: #4CAF50; color: #ffffff; padding: 30px 20px; border-top-left-radius: 12px; border-top-right-radius: 12px;">
                            <h1 style="margin: 0; font-size: 28px; font-weight: 700;">ITBM-SHOP</h1>
                        </div>
                
                        <div style="padding: 30px;">
                            <p style="font-size: 16px; color: #555555; line-height: 1.6;">
                                Hello,
                            </p>
                            <p style="font-size: 16px; color: #555555; line-height: 1.6;">
                                Please verify your email address by clicking the button below.
                            </p>
                
                            <div style="margin: 30px 0;">
                                <a href="%s" style="background-color: #4CAF50; color: #ffffff; padding: 15px 30px; text-decoration: none; font-weight: bold; border-radius: 8px; display: inline-block; font-size: 16px; transition: background-color 0.3s ease;">
                                    Verify Email
                                </a>
                            </div>
                
                            <p style="font-size: 14px; color: #999999; margin-top: 20px;">
                                This link will expire in <b>1 hour</b>.
                            </p>
                        </div>
                
                        <div style="background-color: #f8f9fa; padding: 20px; border-bottom-left-radius: 12px; border-bottom-right-radius: 12px;">
                            <p style="font-size: 14px; color: #777777; margin: 0;">
                                Thanks,<br/>
                                The ITBM-SHOP Team
                            </p>
                        </div>
                
                    </div>
                
                </body>
                </html>
                """.formatted(verificationUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("intsy4.verify@gmail.com");
            helper.setText(htmlContent, true); // true = send as HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}