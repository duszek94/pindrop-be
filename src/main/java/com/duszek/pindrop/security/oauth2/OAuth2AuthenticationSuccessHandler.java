package com.duszek.pindrop.security.oauth2;

import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.repository.UserRepository;
import com.duszek.pindrop.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		String email = oauth2User.getAttribute("email");

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalStateException("OAuth2 user not found after login: " + email));

		String token = jwtService.generateToken(user);

		String redirectUrl = UriComponentsBuilder
				.fromUriString(frontendUrl + "/auth/oauth2/callback")
				.queryParam("token", token)
				.build()
				.toUriString();

		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}
}
