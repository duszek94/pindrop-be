package com.duszek.pindrop.provider.places;

public record PlaceSearchResult(
		String name,
		String region,
		String country,
		String countryCode,
		String displayName,
		double lat,
		double lng,
		String photoUrl,
		String placeType) {

	public static PlaceSearchResult of(
			String name,
			String region,
			String country,
			String countryCode,
			double lat,
			double lng) {
		return of(name, region, country, countryCode, lat, lng, null);
	}

	public static PlaceSearchResult of(
			String name,
			String region,
			String country,
			String countryCode,
			double lat,
			double lng,
			String photoUrl) {
		return of(name, region, country, countryCode, lat, lng, photoUrl, null);
	}

	public static PlaceSearchResult of(
			String name,
			String region,
			String country,
			String countryCode,
			double lat,
			double lng,
			String photoUrl,
			String placeType) {
		return new PlaceSearchResult(
				name,
				region,
				country,
				countryCode,
				PlaceFormatting.formatDisplayName(name, region, country),
				lat,
				lng,
				photoUrl,
				placeType);
	}

	public PlaceSearchResult withPhotoUrl(String photoUrl) {
		return new PlaceSearchResult(name, region, country, countryCode, displayName, lat, lng, photoUrl, placeType);
	}
}
