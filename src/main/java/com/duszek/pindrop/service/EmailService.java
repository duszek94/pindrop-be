package com.duszek.pindrop.service;

import com.duszek.pindrop.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	@Value("${spring.mail.username:}")
	private String fromAddress;

	public void sendPasswordResetEmail(User user, String token) {
		String resetLink = frontendUrl + "/reset-password?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(user.getEmail());
		message.setSubject("Pindrop - Reset your password");
		message.setText(
				"Hello " + user.getFirstName() + ",\n\n"
						+ "We received a request to reset your Pindrop password.\n"
						+ "Use the link below to choose a new password:\n\n"
						+ resetLink + "\n\n"
						+ "If you did not request this, you can safely ignore this email.\n\n"
						+ "— The Pindrop Team");

		mailSender.send(message);
	}
}
