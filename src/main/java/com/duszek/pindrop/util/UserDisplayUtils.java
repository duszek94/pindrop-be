package com.duszek.pindrop.util;

import com.duszek.pindrop.entity.User;

public final class UserDisplayUtils {

	private UserDisplayUtils() {
	}

	public static String fullName(User user) {
		String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
		String last = user.getLastName() != null ? user.getLastName().trim() : "";
		if (first.isEmpty() && last.isEmpty()) {
			return user.getEmail();
		}
		if (first.isEmpty()) {
			return last;
		}
		if (last.isEmpty()) {
			return first;
		}
		return first + " " + last;
	}

	public static String initials(User user) {
		StringBuilder sb = new StringBuilder();
		if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
			sb.append(Character.toUpperCase(user.getFirstName().trim().charAt(0)));
		}
		if (user.getLastName() != null && !user.getLastName().isBlank()) {
			sb.append(Character.toUpperCase(user.getLastName().trim().charAt(0)));
		}
		if (sb.isEmpty() && user.getEmail() != null && !user.getEmail().isBlank()) {
			sb.append(Character.toUpperCase(user.getEmail().charAt(0)));
		}
		return sb.toString();
	}
}
