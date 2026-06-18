package com.duszek.pindrop.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedDestinationResponse {

	private String name;
	private String country;
	private String reason;
	private String imageUrl;
	private Double lat;
	private Double lng;
}
