package com.duszek.pindrop.provider.places;

import com.duszek.pindrop.util.AppLanguage;
import com.duszek.pindrop.util.DebugSessionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class NominatimGeocodingClient {

	private static final Object RATE_LOCK = new Object();
	private static long lastSearchRequestMs = 0;
	private static final long MIN_SEARCH_INTERVAL_MS = 1100;

	private final WebClient webClient = WebClient.builder()
			.baseUrl("https://nominatim.openstreetmap.org")
			.defaultHeader("User-Agent", "Pindrop/1.0 (trip-planning-dev)")
			.build();

	@SuppressWarnings("unchecked")
	public List<PlaceSearchResult> search(String query, int limit) {
		return search(query, limit, "en");
	}

	@SuppressWarnings("unchecked")
	public List<PlaceSearchResult> search(String query, int limit, String language) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		try {
			awaitSearchRateLimit();
			List<Map<String, Object>> results = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/search")
							.queryParam("q", query)
							.queryParam("format", "json")
							.queryParam("limit", Math.max(limit, 12))
							.queryParam("addressdetails", 1)
							.queryParam("dedupe", 1)
							.queryParam("accept-language", AppLanguage.resolve(language))
							.build())
					.retrieve()
					.bodyToMono(List.class)
					.block();

			// #region agent log
			DebugSessionLog.log("H1", "NominatimGeocodingClient.search", "nominatim raw response", new LinkedHashMap<>(Map.of(
					"query", query,
					"language", language,
					"rawCount", results == null ? -1 : results.size())));
			// #endregion

			if (results == null || results.isEmpty()) {
				return List.of();
			}

			String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
			List<ScoredPlace> scored = new ArrayList<>();
			for (Map<String, Object> item : results) {
				PlaceSearchResult place = toPlaceSearchResult(item);
				scored.add(new ScoredPlace(place, scoreResult(place, item, normalizedQuery)));
			}

			scored.sort(Comparator.comparingInt(ScoredPlace::score).reversed());
			List<PlaceSearchResult> mapped = dedupeByNameAndCountry(
					scored.stream().map(ScoredPlace::place).toList())
					.stream()
					.limit(limit)
					.toList();

			// #region agent log
			DebugSessionLog.log("H3", "NominatimGeocodingClient.search", "nominatim mapped results", new LinkedHashMap<>(Map.of(
					"query", query,
					"mappedCount", mapped.size(),
					"topName", mapped.isEmpty() ? "" : mapped.getFirst().name())));
			// #endregion

			return mapped;
		} catch (Exception ex) {
			String errorType = ex instanceof WebClientResponseException response
					? "HTTP_" + response.getStatusCode().value()
					: ex.getClass().getSimpleName();
			log.warn("Nominatim geocoding failed for '{}': {}", query, ex.getMessage());
			// #region agent log
			DebugSessionLog.log("H2", "NominatimGeocodingClient.search", "nominatim exception", new LinkedHashMap<>(Map.of(
					"query", query,
					"error", errorType,
					"message", String.valueOf(ex.getMessage()))));
			// #endregion
			return List.of();
		}
	}

	private static void awaitSearchRateLimit() {
		synchronized (RATE_LOCK) {
			long elapsed = System.currentTimeMillis() - lastSearchRequestMs;
			long waitMs = MIN_SEARCH_INTERVAL_MS - elapsed;
			if (waitMs > 0) {
				try {
					Thread.sleep(waitMs);
				} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
				}
			}
			lastSearchRequestMs = System.currentTimeMillis();
		}
	}

	@SuppressWarnings("unchecked")
	public Optional<PlaceSearchResult> reverse(double lat, double lng) {
		return reverse(lat, lng, "en");
	}

	@SuppressWarnings("unchecked")
	public Optional<PlaceSearchResult> reverse(double lat, double lng, String language) {
		try {
			Map<String, Object> item = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/reverse")
							.queryParam("lat", lat)
							.queryParam("lon", lng)
							.queryParam("format", "json")
							.queryParam("addressdetails", 1)
							.queryParam("accept-language", AppLanguage.resolve(language))
							.build())
					.retrieve()
					.bodyToMono(Map.class)
					.block();

			if (item == null || item.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(toPlaceSearchResult(item));
		} catch (Exception ex) {
			log.warn("Nominatim reverse geocoding failed for {}, {}: {}", lat, lng, ex.getMessage());
			return Optional.empty();
		}
	}

	private static int scoreResult(PlaceSearchResult place, Map<String, Object> item, String normalizedQuery) {
		int score = 0;
		String name = place.name().toLowerCase(Locale.ROOT);
		String rawDisplayName = String.valueOf(item.getOrDefault("display_name", "")).toLowerCase(Locale.ROOT);
		String type = normalizeToken(item.get("type"));
		String placeClass = normalizeToken(item.get("class"));
		String mountainRange = extractAddressField(item, "mountain_range");

		score += scoreTextMatch(name, normalizedQuery);
		score += scoreTextMatch(rawDisplayName, normalizedQuery) / 2;
		score += scoreTextMatch(mountainRange, normalizedQuery);

		if ("mountain_range".equals(type) || "geomorphological-unit".equals(type)) {
			score += 90;
		} else if ("natural".equals(placeClass) || "peak".equals(type) || "ridge".equals(type)) {
			score += 70;
		} else if ("region".equals(place.placeType()) || "lake".equals(place.placeType())) {
			score += 60;
		} else if ("city".equals(place.placeType())) {
			score += 80;
			if (name.equals(normalizedQuery)) {
				score += 60;
			}
		}

		if ("boundary".equals(placeClass) && "administrative".equals(type)) {
			score -= 35;
		}
		if ("village".equals(type) || "hamlet".equals(type)) {
			score -= 25;
		}

		if (normalizedQuery.contains("mountain") || normalizedQuery.contains("mountains")) {
			if ("mountain_range".equals(type) || "geomorphological-unit".equals(type) || "peak".equals(type)) {
				score += 40;
			}
			if ("boundary".equals(placeClass) && "administrative".equals(type)) {
				score -= 40;
			}
		}

		Object importance = item.get("importance");
		if (importance instanceof Number number) {
			score += (int) (number.doubleValue() * 10);
		}

		return score;
	}

	private static int scoreTextMatch(String candidate, String normalizedQuery) {
		if (candidate == null || candidate.isBlank()) {
			return 0;
		}
		String text = candidate.toLowerCase(Locale.ROOT);
		if (text.equals(normalizedQuery)) {
			return 200;
		}
		if (text.contains(normalizedQuery) || normalizedQuery.contains(text)) {
			return 120;
		}
		if (tokensOverlap(text, normalizedQuery)) {
			return 80;
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	private static String extractAddressField(Map<String, Object> item, String field) {
		Object addressObject = item.get("address");
		if (!(addressObject instanceof Map<?, ?> address)) {
			return "";
		}
		Object value = address.get(field);
		return value != null ? String.valueOf(value).toLowerCase(Locale.ROOT) : "";
	}

	private static boolean tokensOverlap(String left, String right) {
		for (String token : right.split("\\s+")) {
			if (token.length() >= 3 && left.contains(token)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private PlaceSearchResult toPlaceSearchResult(Map<String, Object> item) {
		String rawDisplayName = String.valueOf(item.getOrDefault("display_name", ""));
		Map<String, Object> address = item.get("address") instanceof Map<?, ?> addr
				? (Map<String, Object>) addr
				: Map.of();

		String name = resolveName(item, address, rawDisplayName);
		if (name == null) {
			name = "Unknown location";
		}
		String region = resolveRegion(name, address);
		String country = PlaceFormatting.blankToNull(String.valueOf(address.getOrDefault("country", "")));
		String countryCode = PlaceFormatting.blankToNull(String.valueOf(address.getOrDefault("country_code", "")));

		double lat = parseDouble(item.get("lat"));
		double lng = parseDouble(item.get("lon"));

		return PlaceSearchResult.of(
				name,
				region,
				country,
				countryCode,
				lat,
				lng,
				null,
				derivePlaceType(item, address));
	}

	private static String resolveName(Map<String, Object> item, Map<String, Object> address, String rawDisplayName) {
		return firstNonBlank(
				item.get("name"),
				address.get("mountain_range"),
				address.get("geomorphological-unit"),
				address.get("national_park"),
				address.get("peak"),
				address.get("natural"),
				address.get("water"),
				address.get("lake"),
				address.get("reservoir"),
				address.get("city"),
				address.get("town"),
				address.get("village"),
				address.get("municipality"),
				address.get("hamlet"),
				address.get("county"),
				address.get("district"),
				rawDisplayName.split(",")[0]);
	}

	private static String resolveRegion(String name, Map<String, Object> address) {
		String region = firstNonBlank(
				address.get("state_district"),
				address.get("county"),
				address.get("district"),
				address.get("state"),
				address.get("region"),
				address.get("province"));

		if (region != null && region.equalsIgnoreCase(name)) {
			return firstNonBlank(
					address.get("state_district"),
					address.get("county"),
					address.get("district"),
					address.get("state"),
					address.get("province"));
		}

		return region;
	}

	private static String derivePlaceType(Map<String, Object> item, Map<String, Object> address) {
		String type = normalizeToken(item.get("type"));
		String placeClass = normalizeToken(item.get("class"));
		String addresstype = normalizeToken(item.get("addresstype"));

		if (isMountainRangeType(type, address)) {
			return "region";
		}
		if (isMountainType(type, placeClass, address)) {
			return "mountain";
		}
		if (isLakeType(type, placeClass, address)) {
			return "lake";
		}
		if (isCityType(type, placeClass, address, addresstype)) {
			return "city";
		}
		if (isRegionType(type, placeClass, address)) {
			return "region";
		}
		if ("national_park".equals(type) || address.containsKey("national_park")) {
			return "park";
		}
		return type != null ? type : placeClass;
	}

	private static boolean isCityType(
			String type,
			String placeClass,
			Map<String, Object> address,
			String addresstype) {
		if ("city".equals(type) || "town".equals(type) || "village".equals(type) || "hamlet".equals(type)) {
			return true;
		}
		if ("city".equals(addresstype)
				|| "town".equals(addresstype)
				|| "village".equals(addresstype)
				|| "hamlet".equals(addresstype)
				|| "municipality".equals(addresstype)) {
			return true;
		}
		if (address.containsKey("city") || address.containsKey("town") || address.containsKey("municipality")) {
			return "boundary".equals(placeClass) && "administrative".equals(type);
		}
		return false;
	}

	private static boolean isMountainRangeType(String type, Map<String, Object> address) {
		return address.containsKey("mountain_range")
				|| address.containsKey("geomorphological-unit")
				|| "mountain_range".equals(type)
				|| "geomorphological-unit".equals(type);
	}

	private static boolean isMountainType(String type, String placeClass, Map<String, Object> address) {
		return address.containsKey("peak")
				|| "peak".equals(type)
				|| "ridge".equals(type)
				|| "cliff".equals(type)
				|| ("natural".equals(placeClass) && ("peak".equals(type) || "ridge".equals(type)));
	}

	private static boolean isLakeType(String type, String placeClass, Map<String, Object> address) {
		return address.containsKey("water")
				|| address.containsKey("lake")
				|| address.containsKey("reservoir")
				|| "lake".equals(type)
				|| "reservoir".equals(type)
				|| "water".equals(type)
				|| ("waterway".equals(placeClass) && "river".equals(type));
	}

	private static boolean isRegionType(String type, String placeClass, Map<String, Object> address) {
		if (address.containsKey("city") || address.containsKey("town") || address.containsKey("municipality")) {
			return false;
		}
		return "state".equals(type)
				|| "region".equals(type)
				|| "county".equals(type)
				|| "province".equals(type)
				|| ("boundary".equals(placeClass) && "administrative".equals(type) && !address.containsKey("village"));
	}

	private static List<PlaceSearchResult> dedupeByNameAndCountry(List<PlaceSearchResult> results) {
		List<PlaceSearchResult> deduped = new ArrayList<>();
		for (PlaceSearchResult candidate : results) {
			boolean duplicate = deduped.stream().anyMatch(existing -> isSameNameAndCountry(existing, candidate));
			if (!duplicate) {
				deduped.add(candidate);
			}
		}
		return deduped;
	}

	private static boolean isSameNameAndCountry(PlaceSearchResult left, PlaceSearchResult right) {
		return left.name().equalsIgnoreCase(right.name())
				&& safeCountryCode(left.countryCode()).equals(safeCountryCode(right.countryCode()));
	}

	private static String safeCountryCode(String countryCode) {
		return countryCode != null ? countryCode.toLowerCase(Locale.ROOT) : "";
	}

	private static String normalizeToken(Object value) {
		if (value == null) {
			return null;
		}
		String text = PlaceFormatting.blankToNull(String.valueOf(value));
		return text != null ? text.toLowerCase(Locale.ROOT) : null;
	}

	private static String firstNonBlank(Object... values) {
		for (Object value : values) {
			String text = PlaceFormatting.blankToNull(value != null ? String.valueOf(value) : null);
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	private static double parseDouble(Object value) {
		if (value == null) {
			return 0;
		}
		try {
			return Double.parseDouble(String.valueOf(value));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private record ScoredPlace(PlaceSearchResult place, int score) {
	}

	static String classifyPlaceTypeForTest(Map<String, Object> item, Map<String, Object> address) {
		return derivePlaceType(item, address);
	}

	static List<PlaceSearchResult> dedupeByNameAndCountryForTest(List<PlaceSearchResult> results) {
		return dedupeByNameAndCountry(results);
	}
}
