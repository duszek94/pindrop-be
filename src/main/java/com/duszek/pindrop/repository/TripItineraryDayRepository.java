package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.TripItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripItineraryDayRepository extends JpaRepository<TripItineraryDay, Long> {

	List<TripItineraryDay> findByTripIdOrderByDayNumberAsc(Long tripId);

	Optional<TripItineraryDay> findByTripIdAndDayNumber(Long tripId, int dayNumber);

	void deleteByTripId(Long tripId);
}
