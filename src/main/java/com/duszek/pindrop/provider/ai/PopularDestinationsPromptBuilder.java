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
				{"destinations":[{"name":"Barcelona","country":"Spain","placeType":"city"}]}
				Suggest exactly %d popular travel destinations for the period %s to %s (month-to-date).
				Each suggestion must be a well-known destination travelers would recognize and search for
				(e.g. Barcelona, Dolomites, Patagonia, Niagara Falls, Kyoto, Santorini).
				Use short, iconic names for cities, regions, mountain ranges, lakes, or natural wonders.
				Do NOT suggest roads (e.g. A82), administrative subdivisions, embassies, consulates,
				municipal units, or obscure localities.
				Include a mix of cities, regions, lakes, and mountains suited to this time of year.
				placeType must be one of: city, region, lake, mountain.
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
