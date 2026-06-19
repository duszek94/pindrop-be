package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.TravelPace;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePreferencesRequest {

	@NotNull
	private BudgetTier budgetTier;

	@NotNull
	private TravelPace pace;
}
