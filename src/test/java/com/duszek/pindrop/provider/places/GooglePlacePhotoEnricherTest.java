package com.duszek.pindrop.provider.places;

import com.duszek.pindrop.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GooglePlacePhotoEnricherTest {

	@Test
	void enrich_withoutApiKey_leavesPhotoUrlNull() {
		AppProperties properties = new AppProperties();
		properties.getPlaces().setApiKey("");
		GooglePlacePhotoEnricher enricher = new GooglePlacePhotoEnricher(new GooglePlacesClient(properties));

		List<PlaceSearchResult> enriched = enricher.enrich(List.of(
				PlaceSearchResult.of("Warsaw", "Masovian Voivodeship", "Poland", "pl", 52.2297, 21.0122)));

		assertNull(enriched.get(0).photoUrl());
	}

	@Test
	void enrich_keepsExistingPhotoUrl() {
		AppProperties properties = new AppProperties();
		properties.getPlaces().setApiKey("");
		GooglePlacePhotoEnricher enricher = new GooglePlacePhotoEnricher(new GooglePlacesClient(properties));
		PlaceSearchResult withPhoto = PlaceSearchResult.of(
				"Paris", "Île-de-France", "France", "fr", 48.8566, 2.3522, "https://example.com/paris.jpg");

		List<PlaceSearchResult> enriched = enricher.enrich(List.of(withPhoto));

		assertEquals("https://example.com/paris.jpg", enriched.get(0).photoUrl());
	}
}
