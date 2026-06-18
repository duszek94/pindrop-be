package com.duszek.pindrop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuthConfigDiagnostics {

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String clientSecret;

	@EventListener(ApplicationReadyEvent.class)
	void logOAuthConfig() {
		boolean placeholderClientId = clientId == null || clientId.isBlank() || "change-me".equals(clientId);
		boolean placeholderSecret = clientSecret == null || clientSecret.isBlank() || "change-me".equals(clientSecret);

		if (placeholderClientId || placeholderSecret) {
			log.warn(
					"Google OAuth is not configured (invalid_client will occur). "
							+ "Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables. "
							+ "In Google Cloud Console, add authorized redirect URI: "
							+ "http://localhost:8080/login/oauth2/code/google");
		}
	}
}
