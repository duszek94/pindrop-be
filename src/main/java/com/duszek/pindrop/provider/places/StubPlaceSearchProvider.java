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
			new PlaceSearchResult("Tokyo", "Japan", "Tokyo, Japan", 35.6762, 139.6503),
			new PlaceSearchResult("Paris", "France", "Paris, France", 48.8566, 2.3522),
			new PlaceSearchResult("Bali", "Indonesia", "Bali, Indonesia", -8.4095, 115.1889),
			new PlaceSearchResult("New York", "United States", "New York, United States", 40.7128, -74.0060),
			new PlaceSearchResult("Barcelona", "Spain", "Barcelona, Spain", 41.3874, 2.1686),
			new PlaceSearchResult("Lisbon", "Portugal", "Lisbon, Portugal", 38.7223, -9.1393),
			new PlaceSearchResult("Reykjavik", "Iceland", "Reykjavik, Iceland", 64.1466, -21.9426),
			new PlaceSearchResult("Warsaw", "Poland", "Warsaw, Poland", 52.2297, 21.0122));

	@Override
	public List<PlaceSearchResult> search(String query, int limit) {
		if (query == null || query.isBlank()) {
			return POPULAR.stream().limit(limit).toList();
		}
		String normalized = query.toLowerCase(Locale.ROOT);
		List<PlaceSearchResult> localMatches = POPULAR.stream()
				.filter(place -> place.displayName().toLowerCase(Locale.ROOT).contains(normalized)
						|| place.name().toLowerCase(Locale.ROOT).contains(normalized)
						|| place.country().toLowerCase(Locale.ROOT).contains(normalized))
				.limit(limit)
				.toList();

		if (localMatches.isEmpty()) {
			return nominatimGeocodingClient.search(query, limit);
		}
		return localMatches;
	}
}
