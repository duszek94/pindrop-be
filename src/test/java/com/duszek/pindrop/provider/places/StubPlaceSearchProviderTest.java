package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StubPlaceSearchProviderTest {

	@Test
	void searchWarsaw_returnsLocalMatch() {
		StubPlaceSearchProvider provider = new StubPlaceSearchProvider(new NominatimGeocodingClient());

		var results = provider.search("Warsaw", 8);

		assertFalse(results.isEmpty());
		assertEquals("Warsaw", results.get(0).name());
		assertEquals("Masovian Voivodeship", results.get(0).region());
		assertEquals("Poland", results.get(0).country());
		assertEquals("pl", results.get(0).countryCode());
		assertEquals("Warsaw, Masovian Voivodeship, Poland", results.get(0).displayName());
	}
}
