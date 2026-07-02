package com.duszek.pindrop.service;

import com.duszek.pindrop.provider.places.PlaceSearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceLocalizationServiceTest {

	@Mock
	private com.duszek.pindrop.provider.places.NominatimGeocodingClient nominatimGeocodingClient;

	@InjectMocks
	private PlaceLocalizationService placeLocalizationService;

	@Test
	void localizeForSearch_keepsSuggestionNameAndPhoto() {
		PlaceSearchResult santorini = PlaceSearchResult.of(
				"Santorini",
				"South Aegean",
				"Greece",
				"gr",
				36.393,
				25.461,
				"https://lh3.googleusercontent.com/photo.jpg",
				"city");

		PlaceSearchResult localized = placeLocalizationService.localizeForSearch(santorini, "en");

		assertEquals("Santorini", localized.name());
		assertEquals("https://lh3.googleusercontent.com/photo.jpg", localized.photoUrl());
	}

	@Test
	void localize_overwritesNameWithReverseGeocodeResult() {
		when(nominatimGeocodingClient.reverse(anyDouble(), anyDouble(), anyString()))
				.thenReturn(java.util.Optional.of(
						PlaceSearchResult.of("Thira Municipal Unit", "South Aegean", "Greece", "gr", 36.393, 25.461)));

		PlaceSearchResult santorini = PlaceSearchResult.of(
				"Santorini",
				"South Aegean",
				"Greece",
				"gr",
				36.393,
				25.461,
				"https://lh3.googleusercontent.com/photo.jpg",
				"city");

		PlaceSearchResult localized = placeLocalizationService.localize(santorini, "en");

		assertEquals("Thira Municipal Unit", localized.name());
	}
}
