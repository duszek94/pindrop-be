package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.Trip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

	@Query("""
			SELECT t FROM Trip t
			WHERE t.user.id = :userId
			AND (:cursor IS NULL OR t.id < :cursor)
			ORDER BY t.id DESC
			""")
	List<Trip> findByUserIdWithCursor(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);

	Optional<Trip> findByIdAndUserId(Long id, Long userId);

	@Query("""
			SELECT t FROM Trip t
			WHERE t.user.id = :userId
			AND t.lat IS NOT NULL
			AND t.lng IS NOT NULL
			""")
	List<Trip> findGeoMarkersByUserId(@Param("userId") Long userId);
}
