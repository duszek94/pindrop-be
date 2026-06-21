package com.duszek.pindrop.service;

import com.duszek.pindrop.provider.places.NominatimGeocodingClient;
import com.duszek.pindrop.provider.places.PlaceFormatting;
import com.duszek.pindrop.provider.places.PlaceSearchResult;
import com.duszek.pindrop.util.AppLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlaceLocalizationService {

	private final NominatimGeocodingClient nominatimGeocodingClient;

	public PlaceSearchResult localizeForSearch(PlaceSearchResult place, String language) {
		Locale displayLocale = AppLanguage.toLocale(language);
		String country = localizeCountry(place.countryCode(), place.country(), displayLocale);
		String displayName = PlaceFormatting.formatDisplayName(place.name(), place.region(), country);

		return new PlaceSearchResult(
				place.name(),
				place.region(),
				country,
				place.countryCode(),
				displayName,
				place.lat(),
				place.lng(),
				null,
				place.placeType());
	}

	public PlaceSearchResult localize(PlaceSearchResult place, String language) {
		String lang = AppLanguage.resolve(language);
		Locale displayLocale = AppLanguage.toLocale(lang);

		PlaceSearchResult localized = nominatimGeocodingClient.reverse(place.lat(), place.lng(), lang)
				.orElse(place);

		String country = localizeCountry(localized.countryCode(), localized.country(), displayLocale);
		String name = localized.name();
		String region = localized.region();
		String displayName = PlaceFormatting.formatDisplayName(name, region, country);

		return new PlaceSearchResult(
				name,
				region,
				country,
				localized.countryCode(),
				displayName,
				localized.lat(),
				localized.lng(),
				place.photoUrl(),
				localized.placeType());
	}

	private static String localizeCountry(String countryCode, String fallbackCountry, Locale displayLocale) {
		if (countryCode != null && !countryCode.isBlank()) {
			return new Locale("", countryCode.toUpperCase(Locale.ROOT)).getDisplayCountry(displayLocale);
		}
		return fallbackCountry;
	}
}
