package com.duszek.pindrop.provider.places;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GooglePlacePhotoEnricher {

	private final GooglePlacesClient googlePlacesClient;

	public GooglePlacePhotoEnricher(GooglePlacesClient googlePlacesClient) {
		this.googlePlacesClient = googlePlacesClient;
	}

	public List<PlaceSearchResult> enrich(List<PlaceSearchResult> places) {
		if (!googlePlacesClient.isConfigured()) {
			return places;
		}

		List<PlaceSearchResult> enriched = new ArrayList<>(places.size());
		for (PlaceSearchResult place : places) {
			enriched.add(enrichWithQuery(place, buildQuery(place)));
		}
		return enriched;
	}

	public PlaceSearchResult enrichWithQuery(PlaceSearchResult place, String photoQuery) {
		if (!googlePlacesClient.isConfigured()) {
			return place;
		}
		if (hasUsablePhoto(place)) {
			return place;
		}
		return googlePlacesClient.findPhotoUrl(photoQuery)
				.filter(url -> !PhotoUrlValidator.isLikelyMapImage(url))
				.map(place::withPhotoUrl)
				.orElse(place);
	}

	public boolean isConfigured() {
		return googlePlacesClient.isConfigured();
	}

	private static String buildQuery(PlaceSearchResult place) {
		return place.displayName() != null ? place.displayName() : place.name();
	}

	private static boolean hasUsablePhoto(PlaceSearchResult place) {
		return place.photoUrl() != null
				&& !place.photoUrl().isBlank()
				&& !PhotoUrlValidator.isLikelyMapImage(place.photoUrl());
	}
}
