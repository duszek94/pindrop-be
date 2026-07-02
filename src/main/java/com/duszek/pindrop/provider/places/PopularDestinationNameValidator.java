package com.duszek.pindrop.provider.places;

import java.util.regex.Pattern;

public final class PopularDestinationNameValidator {

	private static final Pattern REJECTED = Pattern.compile(
			"(?i)(\\b(a\\d{1,3}|route\\s+\\d|highway|autostrada|municipal unit|consulate|consolat|embassy|"
					+ "prefecture|district office|general de)\\b|^a\\d+$)");

	private PopularDestinationNameValidator() {
	}

	public static boolean isTravelerFriendlyName(String name) {
		if (name == null || name.isBlank()) {
			return false;
		}
		if (name.length() > 48) {
			return false;
		}
		return !REJECTED.matcher(name).find();
	}
}
