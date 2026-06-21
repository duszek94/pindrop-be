package com.duszek.pindrop.provider.places;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotoUrlValidatorTest {

	@Test
	void detectsMapImages() {
		assertTrue(PhotoUrlValidator.isLikelyMapImage(
				"https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Greece_location_map.svg/320px-Greece_location_map.svg.png"));
		assertTrue(PhotoUrlValidator.isLikelyMapImage(
				"https://maps.googleapis.com/maps/api/staticmap?center=0,0"));
	}

	@Test
	void acceptsRegularPhotos() {
		assertFalse(PhotoUrlValidator.isLikelyMapImage(
				"https://images.unsplash.com/photo-1719607526486-96f27a995fcc?w=720"));
		assertFalse(PhotoUrlValidator.isLikelyMapImage(
				"https://lh3.googleusercontent.com/place-photoreference"));
	}

	@Test
	void detectsRemovedUnsplashPhotos() {
		assertTrue(PhotoUrlValidator.isRemovedUnsplashPhoto(
				"https://images.unsplash.com/photo-1613395877344-13d30996eeaa?w=720"));
		assertFalse(PhotoUrlValidator.isRemovedUnsplashPhoto(
				"https://images.unsplash.com/photo-1719607526486-96f27a995fcc?w=720"));
	}
}
