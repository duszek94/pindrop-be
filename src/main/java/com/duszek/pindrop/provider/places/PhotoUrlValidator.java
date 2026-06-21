package com.duszek.pindrop.provider.places;

import java.util.Locale;
import java.util.Set;

public final class PhotoUrlValidator {

	private static final Set<String> REMOVED_UNSPLASH_PHOTOS = Set.of(
			"photo-1613395877344-13d30996eeaa",
			"photo-1534113417760-e3129bfbb240",
			"photo-1489515217757-5fd1be406fef",
			"photo-1529963180234-94d383b4d27c",
			"photo-1599946347372-9b8a791b9a1d",
			"photo-1483347758567-0936f1e55864",
			"photo-1540959733332-eab4deabeeaf",
			"photo-1503614472-8c93d83e9813",
			"photo-1541849546449-79d4d1b6085b",
			"photo-1502602898657-3e91760cbb34",
			"photo-1493976040374-85c8e78512ef",
			"photo-1523906834658-6e24ef2386f9",
			"photo-1555881400-74d7acaacd8b",
			"photo-1605647540924-8522905496bf",
			"photo-1506905925346-21bda4d32df4",
			"photo-1516026672322-bc52d61a55d5",
			"photo-1489749791425-4a977b313225");

	private PhotoUrlValidator() {
	}

	public static boolean isRemovedUnsplashPhoto(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		return REMOVED_UNSPLASH_PHOTOS.stream().anyMatch(url::contains);
	}

	public static boolean isLikelyMapImage(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		String lower = url.toLowerCase(Locale.ROOT);
		return lower.contains("/map")
				|| lower.contains("map_")
				|| lower.contains("_map.")
				|| lower.contains("location_map")
				|| lower.contains("relief_location")
				|| lower.contains("geology_map")
				|| lower.endsWith(".svg")
				|| lower.contains("staticmap")
				|| lower.contains("openstreetmap");
	}
}
