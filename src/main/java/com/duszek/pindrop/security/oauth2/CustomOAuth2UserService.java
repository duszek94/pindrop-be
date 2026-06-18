package com.duszek.pindrop.security.oauth2;

import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.entity.UserRole;
import com.duszek.pindrop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private static final String GOOGLE_PROVIDER = "GOOGLE";

	private final UserRepository userRepository;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);
		Map<String, Object> attributes = oauth2User.getAttributes();

		String email = (String) attributes.get("email");
		String providerId = (String) attributes.get("sub");
		String firstName = (String) attributes.get("given_name");
		String lastName = (String) attributes.get("family_name");

		User user = userRepository.findByEmail(email)
				.map(existing -> linkOAuthAccount(existing, providerId))
				.orElseGet(() -> createOAuthUser(email, providerId, firstName, lastName));

		return new DefaultOAuth2User(
				user.getAuthorities(),
				attributes,
				"email");
	}

	private User linkOAuthAccount(User user, String providerId) {
		if (user.getProvider() == null) {
			user.setProvider(GOOGLE_PROVIDER);
		}
		if (user.getProviderId() == null) {
			user.setProviderId(providerId);
		}
		return userRepository.save(user);
	}

	private User createOAuthUser(String email, String providerId, String firstName, String lastName) {
		User user = new User();
		user.setEmail(email);
		user.setProvider(GOOGLE_PROVIDER);
		user.setProviderId(providerId);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		return userRepository.save(user);
	}
}
