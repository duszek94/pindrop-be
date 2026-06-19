package com.duszek.pindrop.provider.places;

public record PlaceSearchResult(
		String name,
		String country,
		String displayName,
		double lat,
		double lng) {
}
