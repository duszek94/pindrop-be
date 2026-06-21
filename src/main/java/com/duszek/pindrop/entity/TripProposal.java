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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripProposal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trip_id", nullable = false)
	private Trip trip;

	@Enumerated(EnumType.STRING)
	@Column(name = "proposal_type", nullable = false)
	private ProposalType proposalType;

	@Column(nullable = false)
	private String title;

	private String summary;

	@Column(name = "estimated_cost_usd", nullable = false)
	private int estimatedCostUsd;

	@Column(name = "is_recommended", nullable = false)
	private boolean recommended;

	@Column(name = "weather_json", columnDefinition = "TEXT")
	private String weatherJson;

	@Column(name = "highlights_json", columnDefinition = "TEXT")
	private String highlightsJson;

	@Column(name = "cost_breakdown_json", columnDefinition = "TEXT")
	private String costBreakdownJson;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
