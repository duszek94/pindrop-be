package com.duszek.pindrop.provider.places;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CuratedPlaceCatalog {

	private record CuratedEntry(
			String name,
			String region,
			String country,
			String countryCode,
			double lat,
			double lng,
			String placeType,
			List<String> aliases) {

		PlaceSearchResult toPlaceSearchResult() {
			return PlaceSearchResult.of(name, region, country, countryCode, lat, lng, null, placeType);
		}
	}

	private static final List<CuratedEntry> ENTRIES = List.of(
			entry("Tatra Mountains", "Lesser Poland Voivodeship", "Poland", "pl", 49.217, 19.948, "mountain",
					"tatry", "tatra", "tatr"),
			entry("Scottish Highlands", "Scotland", "United Kingdom", "gb", 57.12, -4.71, "region",
					"highlands", "scottish"),
			entry("Lake Bled", "Gorenjska", "Slovenia", "si", 46.364, 14.095, "lake", "bled"),
			entry("Lake Geneva", "Vaud", "Switzerland", "ch", 46.458, 6.561, "lake", "geneva"),
			entry("Plitvice Lakes", "Lika-Senj", "Croatia", "hr", 44.865, 15.582, "lake", "plitvice"),
			entry("Dolomites", "Trentino-Alto Adige", "Italy", "it", 46.411, 11.845, "mountain", "dolomiti"),
			entry("Swiss Alps", "Valais", "Switzerland", "ch", 46.559, 8.561, "mountain", "alps"),
			entry("Atlas Mountains", "Marrakesh-Safi", "Morocco", "ma", 31.062, -7.918, "mountain", "atlas"),
			entry("Amalfi Coast", "Campania", "Italy", "it", 40.634, 14.602, "region", "amalfi"),
			entry("Cappadocia", "Nevşehir", "Turkey", "tr", 38.643, 34.829, "region", "cappadocia"),
			entry("Patagonia", "Santa Cruz", "Argentina", "ar", -49.331, -72.886, "region", "patagonia"),
			entry("Niagara Falls", "Ontario", "Canada", "ca", 43.090, -79.085, "region", "niagara"),
			entry("Lapland", "Lapland", "Finland", "fi", 67.922, 26.505, "region", "lapland"),
			entry("Banff", "Alberta", "Canada", "ca", 51.178, -115.572, "region", "banff"),
			entry("New England", "Massachusetts", "United States", "us", 42.360, -71.058, "region", "england"),
			entry("Santorini", "South Aegean", "Greece", "gr", 36.393, 25.461, "city", "santorini", "thira"),
			entry("Bali", "Bali", "Indonesia", "id", -8.409, 115.188, "city", "bali"),
			entry("Barcelona", "Catalonia", "Spain", "es", 41.387, 2.168, "city", "barcelona"),
			entry("Kyoto", "Kyoto Prefecture", "Japan", "jp", 35.011, 135.768, "city", "kyoto"),
			entry("Lisbon", "Lisbon District", "Portugal", "pt", 38.722, -9.139, "city", "lisbon"),
			entry("Prague", "Prague", "Czech Republic", "cz", 50.075, 14.437, "city", "prague"),
			entry("Paris", "Île-de-France", "France", "fr", 48.856, 2.352, "city", "paris"),
			entry("Tokyo", "Kanto", "Japan", "jp", 35.676, 139.650, "city", "tokyo"),
			entry("Reykjavik", "Capital Region", "Iceland", "is", 64.146, -21.942, "city", "reykjavik"),
			entry("Warsaw", "Masovian Voivodeship", "Poland", "pl", 52.229, 21.012, "city", "warsaw", "warszawa"),
			entry("Gdańsk", "Pomeranian Voivodeship", "Poland", "pl", 54.348, 18.654, "city", "gdansk"),
			entry("Zakopane", "Lesser Poland Voivodeship", "Poland", "pl", 49.299, 19.949, "city", "zakopane"));

	private CuratedPlaceCatalog() {
	}

	static List<PlaceSearchResult> search(String query, int limit) {
		if (query == null || query.isBlank() || query.trim().length() < 3) {
			return List.of();
		}

		String normalized = query.trim().toLowerCase(Locale.ROOT);
		List<ScoredCurated> scored = new ArrayList<>();
		for (CuratedEntry entry : ENTRIES) {
			int score = scoreEntry(entry, normalized);
			if (score > 0) {
				scored.add(new ScoredCurated(entry, score));
			}
		}

		scored.sort((left, right) -> Integer.compare(right.score, left.score));
		return scored.stream()
				.map(item -> item.entry.toPlaceSearchResult())
				.limit(limit)
				.toList();
	}

	private static boolean countryMatches(String placeCountry, String targetCountry) {
		if (targetCountry == null || targetCountry.isBlank()) {
			return true;
		}
		if (placeCountry == null) {
			return false;
		}
		String normalizedPlace = placeCountry.toLowerCase(Locale.ROOT);
		String normalizedTarget = targetCountry.toLowerCase(Locale.ROOT);
		if (normalizedPlace.contains(normalizedTarget) || normalizedTarget.contains(normalizedPlace)) {
			return true;
		}
		return countryAliases(normalizedTarget).stream().anyMatch(normalizedPlace::contains);
	}

	private static List<String> countryAliases(String country) {
		return switch (country) {
			case "united states", "usa", "us" -> List.of("united states", "usa");
			case "united kingdom", "uk", "great britain" -> List.of("united kingdom", "great britain", "scotland");
			case "greece", "hellas" -> List.of("greece", "hellas", "elláda");
			case "spain", "españa" -> List.of("spain", "españa");
			case "italy", "italia" -> List.of("italy", "italia");
			case "switzerland", "schweiz", "suisse" -> List.of("switzerland", "schweiz", "suisse", "svizzera");
			case "poland", "polska" -> List.of("poland", "polska");
			case "indonesia" -> List.of("indonesia");
			case "canada" -> List.of("canada");
			case "argentina" -> List.of("argentina");
			default -> List.of(country);
		};
	}

	public static PlaceSearchResult findForSuggestion(String name, String country) {
		if (name == null || name.isBlank()) {
			return null;
		}

		String targetCountry = country != null ? country.toLowerCase(Locale.ROOT) : "";
		return search(name, 5).stream()
				.filter(place -> countryMatches(place.country(), targetCountry))
				.filter(place -> PopularDestinationNameValidator.isTravelerFriendlyName(place.name()))
				.findFirst()
				.orElse(null);
	}

	private static int scoreEntry(CuratedEntry entry, String normalized) {
		int score = scoreText(entry.name(), normalized);
		for (String alias : entry.aliases()) {
			score = Math.max(score, scoreText(alias, normalized));
		}
		return score;
	}

	private static int scoreText(String candidate, String normalized) {
		String text = candidate.toLowerCase(Locale.ROOT);
		if (text.equals(normalized)) {
			return 200;
		}
		if (text.startsWith(normalized) || normalized.startsWith(text)) {
			return 160;
		}
		if (text.contains(normalized) || normalized.contains(text)) {
			return 120;
		}
		for (String token : normalized.split("\\s+")) {
			if (token.length() >= 3 && text.contains(token)) {
				return 100;
			}
		}
		return 0;
	}

	private static CuratedEntry entry(
			String name,
			String region,
			String country,
			String countryCode,
			double lat,
			double lng,
			String placeType,
			String... aliases) {
		return new CuratedEntry(name, region, country, countryCode, lat, lng, placeType, List.of(aliases));
	}

	private record ScoredCurated(CuratedEntry entry, int score) {
	}
}
