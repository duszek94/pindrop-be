package com.duszek.pindrop.security;

import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
			throw new ForbiddenException("Not authenticated");
		}
		return user;
	}

	public static Long getCurrentUserId() {
		return getCurrentUser().getId();
	}
}
