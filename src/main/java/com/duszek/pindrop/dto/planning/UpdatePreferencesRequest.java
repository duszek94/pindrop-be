package com.duszek.pindrop.dto.planning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePreferencesRequest {

	@NotNull
	@Valid
	private PreferenceProfile preferenceProfile;
}
