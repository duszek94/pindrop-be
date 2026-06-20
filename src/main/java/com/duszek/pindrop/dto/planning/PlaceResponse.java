package com.duszek.pindrop.dto.planning;

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
public class PlaceResponse {

	private String name;
	private String region;
	private String country;
	private String countryCode;
	private String displayName;
	private String photoUrl;
	private double lat;
	private double lng;
}
