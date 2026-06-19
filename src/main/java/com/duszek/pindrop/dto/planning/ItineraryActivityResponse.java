package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryActivityResponse {

	private Long id;
	private LocalTime startTime;
	private ActivityType type;
	private String title;
	private String description;
	private String placeName;
	private Double lat;
	private Double lng;
	private Integer tempC;
}
