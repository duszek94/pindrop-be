package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CuratedPlaceCatalogTest {

	@Test
	void searchPartialTatr_returnsTatraMountains() {
		var results = CuratedPlaceCatalog.search("Tatr", 8);

		assertFalse(results.isEmpty());
		assertEquals("Tatra Mountains", results.getFirst().name());
	}

	@Test
	void searchTatraMountains_returnsTatraMountains() {
		var results = CuratedPlaceCatalog.search("Tatra Mountains", 8);

		assertFalse(results.isEmpty());
		assertEquals("Tatra Mountains", results.getFirst().name());
	}
}
