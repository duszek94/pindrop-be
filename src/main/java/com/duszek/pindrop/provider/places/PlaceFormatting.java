package com.duszek.pindrop.provider.places;

public final class PlaceFormatting {

	private PlaceFormatting() {
	}

	public static String formatDisplayName(String name, String region, String country) {
		StringBuilder builder = new StringBuilder(name);
		if (region != null && !region.isBlank() && !region.equalsIgnoreCase(name)) {
			builder.append(", ").append(region);
		}
		if (country != null && !country.isBlank()) {
			builder.append(", ").append(country);
		}
		return builder.toString();
	}

	public static String blankToNull(String value) {
		if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
			return null;
		}
		return value.trim();
	}
}
