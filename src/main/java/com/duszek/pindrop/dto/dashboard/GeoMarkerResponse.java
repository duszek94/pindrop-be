package com.duszek.pindrop.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeoMarkerResponse {

	private Long tripId;
	private String title;
	private String destination;
	private double lat;
	private double lng;
}
