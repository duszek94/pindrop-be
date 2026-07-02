package com.duszek.pindrop.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TripInterest {

	CULTURE("planTrip.interests.options.culture", "pi-building"),
	HISTORY("planTrip.interests.options.history", "pi-book"),
	ARCHITECTURE("planTrip.interests.options.architecture", "pi-home"),
	FOOD("planTrip.interests.options.food", "pi-shop"),
	NIGHTLIFE("planTrip.interests.options.nightlife", "pi-moon"),
	LOCAL_MARKETS("planTrip.interests.options.localMarkets", "pi-shopping-bag"),
	MUSEUMS("planTrip.interests.options.museums", "pi-images"),
	STREET_ART("planTrip.interests.options.streetArt", "pi-palette"),
	SHOPPING("planTrip.interests.options.shopping", "pi-tag"),
	HIKING("planTrip.interests.options.hiking", "pi-compass"),
	MOUNTAINS("planTrip.interests.options.mountains", "pi-map"),
	BEACHES("planTrip.interests.options.beaches", "pi-globe"),
	WILDLIFE("planTrip.interests.options.wildlife", "pi-heart"),
	SCENIC_VIEWS("planTrip.interests.options.scenicViews", "pi-eye"),
	WATER_ACTIVITIES("planTrip.interests.options.waterActivities", "pi-sun"),
	PHOTOGRAPHY("planTrip.interests.options.photography", "pi-camera"),
	ADVENTURE("planTrip.interests.options.adventure", "pi-bolt"),
	WELLNESS("planTrip.interests.options.wellness", "pi-sparkles");

	private final String labelKey;

	private final String icon;

	public static TripInterest fromName(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return TripInterest.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}
}
