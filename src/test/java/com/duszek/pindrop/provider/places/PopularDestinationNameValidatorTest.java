package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopularDestinationNameValidatorTest {

	@Test
	void rejectsRoadAndAdministrativeNames() {
		assertFalse(PopularDestinationNameValidator.isTravelerFriendlyName("A82"));
		assertFalse(PopularDestinationNameValidator.isTravelerFriendlyName("Thira Municipal Unit"));
		assertFalse(PopularDestinationNameValidator.isTravelerFriendlyName("Consolat General de França a Barcelona"));
	}

	@Test
	void acceptsWellKnownDestinationNames() {
		assertTrue(PopularDestinationNameValidator.isTravelerFriendlyName("Barcelona"));
		assertTrue(PopularDestinationNameValidator.isTravelerFriendlyName("Dolomites"));
		assertTrue(PopularDestinationNameValidator.isTravelerFriendlyName("Patagonia"));
		assertTrue(PopularDestinationNameValidator.isTravelerFriendlyName("Niagara Falls"));
	}

	@Test
	void findForSuggestion_returnsBarcelonaForSpain() {
		PlaceSearchResult place = CuratedPlaceCatalog.findForSuggestion("Barcelona", "Spain");

		assertNotNull(place);
		assertEquals("Barcelona", place.name());
		assertEquals("Spain", place.country());
	}
}
