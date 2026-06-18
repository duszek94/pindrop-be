package com.duszek.pindrop.dto.dashboard;

import com.duszek.pindrop.entity.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

	private Long id;
	private String title;
	private String destination;
	private Double lat;
	private Double lng;
	private LocalDate startDate;
	private LocalDate endDate;
	private TripStatus status;
	private int travelerCount;
	private String coverImageUrl;
	private Integer durationDays;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
