package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.Itinerary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

	@Query("""
			SELECT il.itinerary FROM ItineraryLike il
			WHERE il.user.id = :userId
			ORDER BY il.id DESC
			""")
	List<Itinerary> findFavoritesByUserId(@Param("userId") Long userId);

	@Query("""
			SELECT i FROM Itinerary i
			WHERE i.isPublic = true
			AND (:cursor IS NULL OR i.id < :cursor)
			ORDER BY i.id DESC
			""")
	List<Itinerary> findPublicWithCursor(@Param("cursor") Long cursor, Pageable pageable);

	Optional<Itinerary> findByIdAndIsPublicTrue(Long id);

	@Modifying
	@Query("UPDATE Itinerary i SET i.likeCount = i.likeCount + 1 WHERE i.id = :id")
	int incrementLikeCount(@Param("id") Long id);

	@Modifying
	@Query("UPDATE Itinerary i SET i.likeCount = i.likeCount - 1 WHERE i.id = :id AND i.likeCount > 0")
	int decrementLikeCount(@Param("id") Long id);
}
