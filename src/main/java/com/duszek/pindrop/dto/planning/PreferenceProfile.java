package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.PaceIntensity;
import com.duszek.pindrop.entity.PreferenceCategory;
import com.duszek.pindrop.entity.SpendingPriority;
import com.duszek.pindrop.entity.TransportMode;
import com.duszek.pindrop.entity.TravelPace;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class PreferenceProfile {

	@NotNull
	private BudgetTier budgetStyle;

	@NotNull
	private Map<PreferenceCategory, SpendingPriority> categoryPriorities = new EnumMap<>(PreferenceCategory.class);

	@NotEmpty
	private Set<TransportMode> transportModes;

	private boolean avoidFlyingWhenTrainReasonable;

	@NotNull
	private TravelPace pace;

	private PaceIntensity paceIntensity;

	@Size(max = 500)
	private String additionalRequirements;
}
