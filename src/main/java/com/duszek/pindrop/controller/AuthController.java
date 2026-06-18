package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.auth.AuthResponse;
import com.duszek.pindrop.dto.auth.ForgotPasswordRequest;
import com.duszek.pindrop.dto.auth.LoginRequest;
import com.duszek.pindrop.dto.auth.MessageResponse;
import com.duszek.pindrop.dto.auth.RegisterRequest;
import com.duszek.pindrop.dto.auth.ResetPasswordRequest;
import com.duszek.pindrop.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/forgot-password")
	public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return new MessageResponse("If an account exists for that email, a reset link has been sent.");
	}

	@PostMapping("/reset-password")
	public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return new MessageResponse("Password has been reset successfully.");
	}
}
