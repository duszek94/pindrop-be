package com.duszek.pindrop.provider.places;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlacePhotoEnricher {

	private final GooglePlacePhotoEnricher googlePlacePhotoEnricher;
	private final WikimediaPhotoEnricher wikimediaPhotoEnricher;

	public PlacePhotoEnricher(
			GooglePlacePhotoEnricher googlePlacePhotoEnricher,
			WikimediaPhotoEnricher wikimediaPhotoEnricher) {
		this.googlePlacePhotoEnricher = googlePlacePhotoEnricher;
		this.wikimediaPhotoEnricher = wikimediaPhotoEnricher;
	}

	public List<PlaceSearchResult> enrich(List<PlaceSearchResult> places) {
		List<PlaceSearchResult> enriched = new ArrayList<>(places.size());
		for (PlaceSearchResult place : places) {
			enriched.add(enrichSingle(place, buildQuery(place)));
		}
		return enriched;
	}

	public PlaceSearchResult enrichWithQuery(PlaceSearchResult place, String photoQuery) {
		return enrichSingle(place, photoQuery);
	}

	private PlaceSearchResult enrichSingle(PlaceSearchResult place, String photoQuery) {
		String query = photoQuery != null && !photoQuery.isBlank()
				? photoQuery
				: buildQuery(place);

		PlaceSearchResult googleEnriched = googlePlacePhotoEnricher.enrichWithQuery(place, query);
		if (hasUsablePhoto(googleEnriched)) {
			return googleEnriched;
		}

		return wikimediaPhotoEnricher.findPhotoUrl(place.name(), place.country())
				.filter(url -> !PhotoUrlValidator.isLikelyMapImage(url))
				.map(place::withPhotoUrl)
				.orElse(place);
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
