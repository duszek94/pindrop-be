package com.duszek.pindrop.util;

import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.entity.TransportMode;

import java.util.Set;

public final class PreferenceProfileUtils {

	private PreferenceProfileUtils() {
	}

	public static boolean hasCarMode(PreferenceProfile profile) {
		if (profile == null || profile.getTransportModes() == null) {
			return false;
		}
		Set<TransportMode> modes = profile.getTransportModes();
		return modes.contains(TransportMode.OWN_CAR)
				|| modes.contains(TransportMode.BUDGET_CAR_RENTAL)
				|| modes.contains(TransportMode.PREMIUM_CAR_RENTAL);
	}

	public static String sanitizeAdditionalRequirements(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
	}
}
