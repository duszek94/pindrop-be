package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	long countByUserIdAndReadFalse(Long userId);

	@Query("""
			SELECT n FROM Notification n
			WHERE n.user.id = :userId
			AND (:cursor IS NULL OR n.id < :cursor)
			ORDER BY n.id DESC
			""")
	List<Notification> findByUserIdWithCursor(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);

	Optional<Notification> findByIdAndUserId(Long id, Long userId);

	@Modifying
	@Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
	int markAllReadByUserId(@Param("userId") Long userId);
}
