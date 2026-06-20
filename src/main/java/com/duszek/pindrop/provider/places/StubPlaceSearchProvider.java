package com.duszek.pindrop.provider.places;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "app.places.provider", havingValue = "stub", matchIfMissing = true)
public class StubPlaceSearchProvider implements PlaceSearchProvider {

	private final NominatimGeocodingClient nominatimGeocodingClient;

	public StubPlaceSearchProvider(NominatimGeocodingClient nominatimGeocodingClient) {
		this.nominatimGeocodingClient = nominatimGeocodingClient;
	}

	private static final List<PlaceSearchResult> POPULAR = List.of(
			PlaceSearchResult.of("Tokyo", "Kanto", "Japan", "jp", 35.6762, 139.6503),
			PlaceSearchResult.of("Paris", "Île-de-France", "France", "fr", 48.8566, 2.3522),
			PlaceSearchResult.of("Bali", "Bali", "Indonesia", "id", -8.4095, 115.1889),
			PlaceSearchResult.of("New York", "New York", "United States", "us", 40.7128, -74.0060),
			PlaceSearchResult.of("Barcelona", "Catalonia", "Spain", "es", 41.3874, 2.1686),
			PlaceSearchResult.of("Lisbon", "Lisbon District", "Portugal", "pt", 38.7223, -9.1393),
			PlaceSearchResult.of("Reykjavik", "Capital Region", "Iceland", "is", 64.1466, -21.9426),
			PlaceSearchResult.of("Warsaw", "Masovian Voivodeship", "Poland", "pl", 52.2297, 21.0122));

	@Override
	public List<PlaceSearchResult> search(String query, int limit) {
		if (query == null || query.isBlank()) {
			return POPULAR.stream().limit(limit).toList();
		}
		String normalized = query.toLowerCase(Locale.ROOT);
		List<PlaceSearchResult> localMatches = POPULAR.stream()
				.filter(place -> matchesQuery(place, normalized))
				.limit(limit)
				.toList();

		if (localMatches.isEmpty()) {
			return nominatimGeocodingClient.search(query, limit);
		}
		return localMatches;
	}

	private static boolean matchesQuery(PlaceSearchResult place, String normalized) {
		return contains(place.displayName(), normalized)
				|| contains(place.name(), normalized)
				|| contains(place.region(), normalized)
				|| contains(place.country(), normalized);
	}

	private static boolean contains(String value, String normalized) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(normalized);
	}
}
