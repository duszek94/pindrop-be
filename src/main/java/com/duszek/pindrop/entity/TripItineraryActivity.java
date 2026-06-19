package com.duszek.pindrop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "itinerary_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripItineraryActivity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "day_id", nullable = false)
	private TripItineraryDay day;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "activity_type", nullable = false)
	private ActivityType activityType;

	@Column(nullable = false)
	private String title;

	private String description;

	@Column(name = "place_name")
	private String placeName;

	private Double lat;

	private Double lng;

	@Column(name = "weather_json", columnDefinition = "TEXT")
	private String weatherJson;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;
}
