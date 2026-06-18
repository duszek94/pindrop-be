package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.auth.AuthResponse;
import com.duszek.pindrop.dto.auth.ForgotPasswordRequest;
import com.duszek.pindrop.dto.auth.LoginRequest;
import com.duszek.pindrop.dto.auth.RegisterRequest;
import com.duszek.pindrop.dto.auth.ResetPasswordRequest;
import com.duszek.pindrop.entity.PasswordResetToken;
import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.entity.UserRole;
import com.duszek.pindrop.exception.BadRequestException;
import com.duszek.pindrop.repository.PasswordResetTokenRepository;
import com.duszek.pindrop.repository.UserRepository;
import com.duszek.pindrop.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final int RESET_TOKEN_VALIDITY_HOURS = 1;

	private final UserRepository userRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final EmailService emailService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BadRequestException("Email is already registered");
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setRole(UserRole.USER);
		user.setEnabled(true);

		User savedUser = userRepository.save(user);
		return buildAuthResponse(savedUser);
	}

	public AuthResponse login(LoginRequest request) {
		AuthenticationManager authManager = authenticationManager;
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				request.getEmail(),
				request.getPassword());

		authManager.authenticate(authToken);

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new BadRequestException("Invalid credentials"));

		return buildAuthResponse(user);
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
			passwordResetTokenRepository.deleteAllByUser(user);

			PasswordResetToken resetToken = new PasswordResetToken();
			resetToken.setToken(UUID.randomUUID().toString());
			resetToken.setUser(user);
			resetToken.setExpiryDate(LocalDateTime.now().plusHours(RESET_TOKEN_VALIDITY_HOURS));
			resetToken.setUsed(false);

			passwordResetTokenRepository.save(resetToken);
			emailService.sendPasswordResetEmail(user, resetToken.getToken());
		});
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

		if (resetToken.isUsed()) {
			throw new BadRequestException("Reset token has already been used");
		}

		if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new BadRequestException("Reset token has expired");
		}

		User user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);

		resetToken.setUsed(true);
		passwordResetTokenRepository.save(resetToken);
	}

	private AuthResponse buildAuthResponse(User user) {
		String token = jwtService.generateToken(user);
		return AuthResponse.builder()
				.accessToken(token)
				.tokenType("Bearer")
				.email(user.getEmail())
				.firstName(user.getFirstName())
				.build();
	}
}
