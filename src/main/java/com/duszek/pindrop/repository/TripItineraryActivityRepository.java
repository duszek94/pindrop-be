package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.TripItineraryActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripItineraryActivityRepository extends JpaRepository<TripItineraryActivity, Long> {

	List<TripItineraryActivity> findByDayIdOrderBySortOrderAsc(Long dayId);

	Optional<TripItineraryActivity> findByIdAndDay_Trip_Id(Long activityId, Long tripId);

	void deleteByDay_Trip_Id(Long tripId);
}
