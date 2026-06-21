package com.duszek.pindrop.util;

import java.util.List;
import java.util.Locale;

public final class AppLanguage {

	public static final List<String> SUPPORTED = List.of("en", "pl");

	private AppLanguage() {
	}

	public static String resolve(String acceptLanguage) {
		if (acceptLanguage == null || acceptLanguage.isBlank()) {
			return "en";
		}

		for (String token : acceptLanguage.split(",")) {
			String tag = token.trim().split(";")[0].trim().toLowerCase(Locale.ROOT);
			if (tag.isEmpty()) {
				continue;
			}
			String primary = tag.split("-")[0];
			if (SUPPORTED.contains(primary)) {
				return primary;
			}
		}
		return "en";
	}

	public static Locale toLocale(String language) {
		return Locale.forLanguageTag(resolve(language));
	}

	public static String languageName(String language) {
		return switch (resolve(language)) {
			case "pl" -> "Polish";
			default -> "English";
		};
	}
}
