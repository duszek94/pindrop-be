package com.duszek.pindrop.provider.places;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NominatimGeocodingClient {

	private final WebClient webClient = WebClient.builder()
			.baseUrl("https://nominatim.openstreetmap.org")
			.defaultHeader("User-Agent", "Pindrop/1.0 (trip-planning-dev)")
			.build();

	@SuppressWarnings("unchecked")
	public List<PlaceSearchResult> search(String query, int limit) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		try {
			List<Map<String, Object>> results = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/search")
							.queryParam("q", query)
							.queryParam("format", "json")
							.queryParam("limit", limit)
							.queryParam("addressdetails", 1)
							.build())
					.retrieve()
					.bodyToMono(List.class)
					.block();

			if (results == null || results.isEmpty()) {
				return List.of();
			}

			List<PlaceSearchResult> places = new ArrayList<>();
			for (Map<String, Object> item : results) {
				places.add(toPlaceSearchResult(item));
			}
			return places;
		} catch (Exception ex) {
			log.warn("Nominatim geocoding failed for '{}': {}", query, ex.getMessage());
			return List.of();
		}
	}

	@SuppressWarnings("unchecked")
	private PlaceSearchResult toPlaceSearchResult(Map<String, Object> item) {
		String rawDisplayName = String.valueOf(item.getOrDefault("display_name", ""));
		Map<String, Object> address = item.get("address") instanceof Map<?, ?> addr
				? (Map<String, Object>) addr
				: Map.of();

		String name = firstNonBlank(
				address.get("city"),
				address.get("town"),
				address.get("village"),
				address.get("municipality"),
				item.get("name"),
				rawDisplayName.split(",")[0]);
		String region = firstNonBlank(
				address.get("state"),
				address.get("region"),
				address.get("state_district"),
				address.get("county"),
				address.get("province"));
		String country = PlaceFormatting.blankToNull(String.valueOf(address.getOrDefault("country", "")));
		String countryCode = PlaceFormatting.blankToNull(String.valueOf(address.getOrDefault("country_code", "")));

		double lat = parseDouble(item.get("lat"));
		double lng = parseDouble(item.get("lon"));

		return PlaceSearchResult.of(name, region, country, countryCode, lat, lng);
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
}
