package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NominatimGeocodingClientTest {

	@Test
	void dedupeByNameAndCountry_keepsSingleCityResult() {
		PlaceSearchResult first = PlaceSearchResult.of(
				"Gdańsk", "Pomeranian Voivodeship", "Poland", "pl", 54.348, 18.654, null, "city");
		PlaceSearchResult second = PlaceSearchResult.of(
				"Gdańsk", "Pomeranian Voivodeship", "Poland", "pl", 54.428, 18.798, null, "city");

		List<PlaceSearchResult> deduped = NominatimGeocodingClient.dedupeByNameAndCountryForTest(
				List.of(first, second));

		assertThat(deduped).hasSize(1);
		assertThat(deduped.getFirst().placeType()).isEqualTo("city");
	}

	@Test
	void classifyPlaceType_marksNominatimCityBoundaryAsCity() {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("class", "boundary");
		item.put("type", "administrative");
		item.put("addresstype", "city");
		item.put("name", "Gdańsk");

		Map<String, Object> address = Map.of(
				"city", "Gdańsk",
				"state", "województwo pomorskie",
				"country", "Polska",
				"country_code", "pl");

		assertThat(NominatimGeocodingClient.classifyPlaceTypeForTest(item, address)).isEqualTo("city");
	}

	@Test
	void classifyPlaceType_marksVoivodeshipAsRegion() {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("class", "boundary");
		item.put("type", "administrative");
		item.put("addresstype", "state");

		Map<String, Object> address = Map.of(
				"state", "województwo pomorskie",
				"country", "Polska",
				"country_code", "pl");

		assertThat(NominatimGeocodingClient.classifyPlaceTypeForTest(item, address)).isEqualTo("region");
	}
}
