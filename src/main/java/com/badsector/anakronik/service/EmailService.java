package com.badsector.anakronik.service;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Hesabınızı Doğrulayın";
        String confirmationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String message = "Anakronik'e kaydolduğunuz için teşekkürler! Lütfen hesabınızı doğrulamak için aşağıdaki linke tıklayın:";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message + "\n" + confirmationUrl);
        mailSender.send(email);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Anakronik - Şifre Sıfırlama Talebi";
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String message = "Şifrenizi sıfırlamak için bir talepte bulundunuz. Aşağıdaki linke tıklayarak yeni şifrenizi belirleyebilirsiniz:\n\n"
                + resetUrl + "\n\n"
                + "Eğer bu talebi siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}