package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GooglePlacesClientTest {

	@Test
	void extractPhotoUri_readsPhotoUriFromMediaResponse() {
		Map<String, Object> body = Map.of(
				"name", "places/ChIJ2fzCmcW7j4AR2JzfXBBoh6E/photos/AUacShh3/media",
				"photoUri", "https://lh3.googleusercontent.com/a-/AD_cFT-b=s100-p-k-no-mo");

		assertEquals(
				"https://lh3.googleusercontent.com/a-/AD_cFT-b=s100-p-k-no-mo",
				GooglePlacesClient.extractPhotoUri(body));
	}

	@Test
	void extractPhotoUri_returnsNullWhenMissing() {
		assertNull(GooglePlacesClient.extractPhotoUri(Map.of("name", "places/abc/photos/xyz/media")));
		assertNull(GooglePlacesClient.extractPhotoUri(null));
	}

	@Test
	void normalizePhotoUri_addsHttpsForProtocolRelativeUrls() {
		assertEquals(
				"https://lh3.googleusercontent.com/a-/AD_cFT-b=s100-p-k-no-mo",
				GooglePlacesClient.normalizePhotoUri("//lh3.googleusercontent.com/a-/AD_cFT-b=s100-p-k-no-mo"));
	}

	@Test
	void normalizePhotoUri_keepsAbsoluteUrls() {
		assertEquals(
				"https://lh3.googleusercontent.com/photo.jpg",
				GooglePlacesClient.normalizePhotoUri("https://lh3.googleusercontent.com/photo.jpg"));
	}
}
