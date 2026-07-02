package com.duszek.pindrop.entity;

import com.duszek.pindrop.dto.planning.PreferenceProfile;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private String title;

	private String destination;

	@Column(name = "place_type")
	private String placeType;

	private Double lat;

	private Double lng;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TripStatus status = TripStatus.PLANNING;

	@Column(name = "traveler_count", nullable = false)
	private int travelerCount = 1;

	@Column(name = "cover_image_url")
	private String coverImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "budget_tier")
	private BudgetTier budgetTier;

	@Enumerated(EnumType.STRING)
	private TravelPace pace;

	@Column(name = "preference_profile", columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private PreferenceProfile preferenceProfile;

	@Column(columnDefinition = "TEXT[]")
	@JdbcTypeCode(SqlTypes.ARRAY)
	private List<String> interests;

	@Enumerated(EnumType.STRING)
	@Column(name = "selected_proposal_type")
	private ProposalType selectedProposalType;

	@Column(name = "wizard_step")
	private Short wizardStep;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
