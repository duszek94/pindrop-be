package com.duszek.pindrop.provider.ai;

public record PopularDestinationSuggestion(
		String name,
		String country,
		String placeType,
		String photoUrl) {

	public PopularDestinationSuggestion(String name, String country, String placeType) {
		this(name, country, placeType, null);
	}
}
