package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.ItineraryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryLikeRepository extends JpaRepository<ItineraryLike, Long> {

	Optional<ItineraryLike> findByItineraryIdAndUserId(Long itineraryId, Long userId);

	boolean existsByItineraryIdAndUserId(Long itineraryId, Long userId);
}
