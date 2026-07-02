package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.planning.InterestSuggestionResponse;
import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.PreferenceCategory;
import com.duszek.pindrop.entity.SpendingPriority;
import com.duszek.pindrop.entity.TransportMode;
import com.duszek.pindrop.entity.TravelPace;
import com.duszek.pindrop.entity.Trip;
import com.duszek.pindrop.entity.TripInterest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterestSuggestionServiceTest {

	private InterestSuggestionService service;

	@BeforeEach
	void setUp() {
		service = new InterestSuggestionService();
	}

	@Test
	void cityDestination_excludesBeachesAndMountains() {
		Trip trip = trip("Warsaw, Poland", "city", relaxedProfile());

		List<InterestSuggestionResponse> suggestions = service.suggest(trip);

		assertEquals(6, suggestions.size());
		Set<String> ids = ids(suggestions);
		assertFalse(ids.contains(TripInterest.BEACHES.name()));
		assertFalse(ids.contains(TripInterest.MOUNTAINS.name()));
		assertFalse(ids.contains(TripInterest.WATER_ACTIVITIES.name()));
	}

	@Test
	void mountainDestination_excludesNightlifeAndBeaches() {
		Trip trip = trip("Dolomites, Italy", "mountain", activeProfile());

		List<InterestSuggestionResponse> suggestions = service.suggest(trip);

		assertEquals(6, suggestions.size());
		Set<String> ids = ids(suggestions);
		assertFalse(ids.contains(TripInterest.NIGHTLIFE.name()));
		assertFalse(ids.contains(TripInterest.BEACHES.name()));
		assertFalse(ids.contains(TripInterest.SHOPPING.name()));
	}

	@Test
	void alwaysReturnsSixSuggestions() {
		Trip trip = trip("Kraków, Poland", "city", balancedProfile());

		assertEquals(6, service.suggest(trip).size());
	}

	@Test
	void activePaceBoostsHikingAboveCultureForMountainTrip() {
		Trip activeTrip = trip("Tatra Mountains, Poland", "mountain", activeProfile());
		Trip relaxedTrip = trip("Tatra Mountains, Poland", "mountain", relaxedProfile());

		List<String> activeOrder = service.suggest(activeTrip).stream()
				.map(InterestSuggestionResponse::getId)
				.toList();
		List<String> relaxedOrder = service.suggest(relaxedTrip).stream()
				.map(InterestSuggestionResponse::getId)
				.toList();

		assertTrue(activeOrder.indexOf(TripInterest.HIKING.name()) < activeOrder.indexOf(TripInterest.CULTURE.name()));
		assertTrue(relaxedOrder.indexOf(TripInterest.WELLNESS.name()) < relaxedOrder.indexOf(TripInterest.HIKING.name()));
	}

	@Test
	void investInAttractionsBoostsCultureAndMuseums() {
		PreferenceProfile profile = balancedProfile();
		profile.getCategoryPriorities().put(PreferenceCategory.ATTRACTIONS, SpendingPriority.INVEST);

		List<InterestSuggestionResponse> suggestions = service.suggest(trip("Paris, France", "city", profile));

		List<String> topTwo = suggestions.stream()
				.limit(2)
				.map(InterestSuggestionResponse::getId)
				.toList();
		assertTrue(topTwo.contains(TripInterest.CULTURE.name()) || topTwo.contains(TripInterest.MUSEUMS.name()));
	}

	private static Trip trip(String destination, String placeType, PreferenceProfile profile) {
		Trip trip = new Trip();
		trip.setDestination(destination);
		trip.setPlaceType(placeType);
		trip.setPreferenceProfile(profile);
		return trip;
	}

	private static PreferenceProfile relaxedProfile() {
		return profile(TravelPace.RELAXED, BudgetTier.MID_RANGE);
	}

	private static PreferenceProfile activeProfile() {
		return profile(TravelPace.ACTIVE, BudgetTier.MID_RANGE);
	}

	private static PreferenceProfile balancedProfile() {
		return profile(TravelPace.BALANCED, BudgetTier.MID_RANGE);
	}

	private static PreferenceProfile profile(TravelPace pace, BudgetTier budget) {
		PreferenceProfile profile = new PreferenceProfile();
		profile.setBudgetStyle(budget);
		profile.setPace(pace);
		profile.setTransportModes(Set.of(TransportMode.PUBLIC_TRANSIT));
		Map<PreferenceCategory, SpendingPriority> priorities = new EnumMap<>(PreferenceCategory.class);
		for (PreferenceCategory category : PreferenceCategory.values()) {
			priorities.put(category, SpendingPriority.BALANCED);
		}
		profile.setCategoryPriorities(priorities);
		return profile;
	}

	private static Set<String> ids(List<InterestSuggestionResponse> suggestions) {
		return suggestions.stream()
				.map(InterestSuggestionResponse::getId)
				.collect(Collectors.toSet());
	}
}
