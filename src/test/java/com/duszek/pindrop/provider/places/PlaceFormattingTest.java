package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceFormattingTest {

	@Test
	void formatDisplayName_includesRegionWhenPresent() {
		assertEquals(
				"Warsaw, Masovian Voivodeship, Poland",
				PlaceFormatting.formatDisplayName("Warsaw", "Masovian Voivodeship", "Poland"));
	}

	@Test
	void formatDisplayName_omitsDuplicateRegionName() {
		assertEquals("Bali, Indonesia", PlaceFormatting.formatDisplayName("Bali", "Bali", "Indonesia"));
	}
}
