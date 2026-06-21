package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.util.AppLanguage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class PopularDestinationsPromptBuilder {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

	private PopularDestinationsPromptBuilder() {
	}

	public static String buildPrompt(PopularDestinationsRequest request) {
		return """
				You are a travel trends analyst. Return JSON only with this shape:
				{"destinations":[{"name":"Lake Bled","country":"Slovenia","placeType":"lake"}]}
				Suggest exactly %d popular travel destinations for the period %s to %s (month-to-date).
				Include a mix of cities, regions, lakes, and mountains that travelers are choosing this time of year.
				Use real, geocodable place names. placeType must be one of: city, region, lake, mountain.
				Write every destination name and country in %s.
				Prefer variety across countries and place types. Do not include commentary outside JSON.
				""".formatted(
				request.limit(),
				format(request.periodStart()),
				format(request.periodEnd()),
				AppLanguage.languageName(request.language()));
	}

	private static String format(LocalDate date) {
		return date.format(DATE_FORMAT);
	}
}
