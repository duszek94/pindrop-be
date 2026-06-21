package com.duszek.pindrop.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppLanguageTest {

	@Test
	void resolvesPrimarySupportedLanguage() {
		assertEquals("pl", AppLanguage.resolve("pl-PL,pl;q=0.9,en;q=0.8"));
		assertEquals("en", AppLanguage.resolve("en-US,en;q=0.9"));
		assertEquals("en", AppLanguage.resolve(null));
	}
}
