package com.duszek.pindrop.provider.ai;

import java.time.LocalDate;

public record PopularDestinationsRequest(
		LocalDate periodStart,
		LocalDate periodEnd,
		int limit,
		String language) {
}
