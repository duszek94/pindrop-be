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
			if (place.photoUrl() != null && !place.photoUrl().isBlank()) {
				enriched.add(place);
				continue;
			}
			String query = place.displayName() != null ? place.displayName() : place.name();
			enriched.add(googlePlacesClient.findPhotoUrl(query)
					.map(place::withPhotoUrl)
					.orElse(place));
		}
		return enriched;
	}
}
