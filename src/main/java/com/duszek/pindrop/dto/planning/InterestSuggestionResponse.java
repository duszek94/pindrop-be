package com.duszek.pindrop.dto.planning;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterestSuggestionResponse {

	private String id;

	private String labelKey;

	private String icon;

	private boolean recommended;
}
