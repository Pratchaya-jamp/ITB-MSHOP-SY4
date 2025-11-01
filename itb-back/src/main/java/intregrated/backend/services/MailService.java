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
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
                    </style>
                </head>
                <body style="font-family: 'Inter', sans-serif; background-color: #eef2f6; margin: 0; padding: 60px 20px; text-align: center; color: #333;">

                    <div style="max-width: 550px; margin: auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 12px 35px rgba(0, 0, 0, 0.08); overflow: hidden; text-align: left; border: 1px solid #e3e8ed;">

                        <div style="background-color: #f8fbfd; padding: 30px 45px; text-align: center; border-bottom: 1px solid #eef2f6;">
                            <h1 style="font-size: 30px; font-weight: 700; color: #38a169; margin: 0; letter-spacing: -0.5px;">ITBM-SHOP</h1>
                            <p style="font-size: 15px; color: #8898aa; margin-top: 8px;">Email Verification</p>
                        </div>

                        <div style="padding: 40px 45px;">

                            <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 20px;">
                                Hello there,
                            </p>
                            <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 35px;">
                                Thank you for joining ITBM-SHOP! To activate your account, please verify your email address by clicking the button below.
                            </p>

                            <div style="margin: 40px 0; text-align: center;">
                                <a href="%s" style="background-color: #4CAF50; color: #ffffff; padding: 18px 40px; text-decoration: none; font-weight: 600; border-radius: 10px; display: inline-block; font-size: 18px; transition: background-color 0.3s ease, transform 0.2s ease; box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);">
                                    Verify Your Email Address
                                </a>
                            </div>

                            <p style="font-size: 15px; color: #aab8c2; margin-top: 40px; text-align: center;">
                                This link is valid for <b style="color: #6d7f95;">1 hour</b>. If you did not sign up for ITBM-SHOP, please ignore this email.
                            </p>

                            <div style="border-top: 1px solid #eef2f6; margin-top: 45px; padding-top: 30px; text-align: center;">
                                <p style="font-size: 14px; color: #8898aa; margin: 0; line-height: 1.7;">
                                    Warm regards,<br/>
                                    The ITBM-SHOP Team
                                </p>
                                <p style="font-size: 12px; color: #ccd6df; margin-top: 15px;">
                                    © 2023 ITBM-SHOP. All rights reserved.
                                </p>
                            </div>

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

    public void sendVerificationPassword(String to, String jwtToken) {
        String subject = "Password Reset Request - ITBM-SHOP";
        String verificationUrl = "https://intproj24.sit.kmutt.ac.th/sy4/verify-password/?token=" + jwtToken;

        // HTML content with styled button
        String htmlContent = """
                <html>
                <head>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
                    </style>
                </head>
                <body style="font-family: 'Inter', sans-serif; background-color: #eef2f6; margin: 0; padding: 60px 20px; text-align: center; color: #333;">
                    
                    <div style="max-width: 550px; margin: auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 12px 35px rgba(0, 0, 0, 0.08); overflow: hidden; text-align: left; border: 1px solid #e3e8ed;">
                        
                        <div style="background-color: #f8fbfd; padding: 30px 45px; text-align: center; border-bottom: 1px solid #eef2f6;">
                            <h1 style="font-size: 30px; font-weight: 700; color: #38a169; margin: 0; letter-spacing: -0.5px;">ITBM-SHOP</h1>
                            <p style="font-size: 15px; color: #8898aa; margin-top: 8px;">Password Reset Request</p>
                        </div>
                    
                        <div style="padding: 40px 45px;">
                        
                            <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 20px;">
                                Hello,
                            </p>
                            <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 35px;">
                                We received a request to reset your password. To proceed, please click the button below. No changes will be made to your account until you act.
                            </p>
                    
                            <div style="margin: 40px 0; text-align: center;">
                                <a href="%s" style="background-color: #4CAF50; color: #ffffff; padding: 18px 40px; text-decoration: none; font-weight: 600; border-radius: 10px; display: inline-block; font-size: 18px; transition: background-color 0.3s ease, transform 0.2s ease; box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4);">
                                    Reset Your Password
                                </a>
                            </div>
                    
                            <p style="font-size: 15px; color: #aab8c2; margin-top: 40px; text-align: center;">
                                This link is valid for <b style="color: #6d7f95;">10 Minutes</b>. If you did not request this change, please ignore this email.
                            </p>
                            
                            <div style="border-top: 1px solid #eef2f6; margin-top: 45px; padding-top: 30px; text-align: center;">
                                <p style="font-size: 14px; color: #8898aa; margin: 0; line-height: 1.7;">
                                    Warm regards,<br/>
                                    The ITBM-SHOP Team
                                </p>
                                <p style="font-size: 12px; color: #ccd6df; margin-top: 15px;">
                                    © 2023 ITBM-SHOP. All rights reserved.
                                </p>
                            </div>
                            
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

    public void sendPasswordNotify(String to) {
        String subject = "Your password has been changed - ITBM-SHOP";

        // HTML content with styled button
        String htmlContent = """
                <html>
                <head>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
                    </style>
                </head>
                <body style="font-family: 'Inter', sans-serif; background-color: #eef2f6; margin: 0; padding: 60px 20px; text-align: center; color: #333;">

                    <div style="max-width: 550px; margin: auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 12px 35px rgba(0, 0, 0, 0.08); overflow: hidden; text-align: left; border: 1px solid #e3e8ed;">

                        <div style="background-color: #f8fbfd; padding: 30px 45px; text-align: center; border-bottom: 1px solid #eef2f6;">
                            <h1 style="font-size: 30px; font-weight: 700; color: #38a169; margin: 0; letter-spacing: -0.5px;">ITBM-SHOP</h1>
                            <p style="font-size: 15px; color: #8898aa; margin-top: 8px;">Password Changed Successfully</p>
                        </div>

                        <div style="padding: 40px 45px;">

                            <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 20px;">
                                Hello,
                            </p>
                            <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 35px;">
                                This is to confirm that the password for your ITBM-SHOP account was successfully changed.
                            </p>

                            <p style="font-size: 15px; color: #aab8c2; margin-top: 40px; text-align: center; line-height: 1.7;">
                                If you did not make this change, please contact our support team immediately to secure your account.
                            </p>

                            <div style="border-top: 1px solid #eef2f6; margin-top: 45px; padding-top: 30px; text-align: center;">
                                <p style="font-size: 14px; color: #8898aa; margin: 0; line-height: 1.7;">
                                    Warm regards,<br/>
                                    The ITBM-SHOP Team
                                </p>
                                <p style="font-size: 12px; color: #ccd6df; margin-top: 15px;">
                                    © 2023 ITBM-SHOP. All rights reserved.
                                </p>
                            </div>

                        </div>

                    </div>

                </body>
                </html>
                """;

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
            throw new RuntimeException("Failed to send notify email", e);
        }
    }

    public void sendOtpEmail(String to, String otp) {
        String subject = "Your OTP Code - ITBM-SHOP";

        // 💡 ปรับปรุง HTML:
        // 1. ลบ <head>, <style> และ @import ที่ไม่จำเป็นออก
        // 2. เปลี่ยน font-family ใน <body> เป็น web-safe (Arial, Helvetica)
        String htmlContent = """
        <html>
        <body style="font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif; background-color: #eef2f6; margin: 0; padding: 60px 20px; text-align: center; color: #333;">

            <div style="max-width: 550px; margin: auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 12px 35px rgba(0, 0, 0, 0.08); overflow: hidden; text-align: left; border: 1px solid #e3e8ed;">

                <div style="background-color: #f8fbfd; padding: 30px 45px; text-align: center; border-bottom: 1px solid #eef2f6;">
                    <h1 style="font-size: 30px; font-weight: 700; color: #38a169; margin: 0; letter-spacing: -0.5px;">ITBM-SHOP</h1>
                    <p style="font-size: 15px; color: #8898aa; margin-top: 8px;">Your OTP Code</p>
                </div>

                <div style="padding: 40px 45px;">
                    <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 25px;">
                        Hello,
                    </p>
                    <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 30px;">
                        Here is your One-Time Password (OTP) to complete your verification. Please use the following code:
                    </p>

                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 40px; font-weight: 700; color: #333; letter-spacing: 5px; background-color: #f8fbfd; padding: 15px 25px; border-radius: 8px; display: inline-block;">
                            {OTP_CODE}
                        </span>
                    </div>

                    <p style="font-size: 17px; line-height: 1.8; color: #52667a; margin-bottom: 35px; text-align: center;">
                        This code will expire in <strong>5 minutes</strong>.
                    </p>
                    <p style="font-size: 15px; color: #aab8c2; margin-top: 40px; text-align: center; line-height: 1.7;">
                        For your security, never share this code with anyone. If you did not request this, please ignore this email.
                    </p>

                    <div style="border-top: 1px solid #eef2f6; margin-top: 45px; padding-top: 30px; text-align: center;">
                        <p style="font-size: 14px; color: #8898aa; margin: 0; line-height: 1.7;">
                            Warm regards,<br/>
                            The ITBM-SHOP Team
                        </p>
                        <p style="font-size: 12px; color: #ccd6df; margin-top: 15px;">
                            © 2023 ITBM-SHOP. All rights reserved.
                        </p>
                    </div>
                </div>
            </div>
        </body>
        </html>
    """;

        // แทนที่ Placeholder ด้วยค่า OTP จริง
        String finalHtmlContent = htmlContent.replace("{OTP_CODE}", otp);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("intsy4.verify@gmail.com");
            helper.setText(finalHtmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}