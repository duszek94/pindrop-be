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
	private String country;
	private String displayName;
	private double lat;
	private double lng;
}
