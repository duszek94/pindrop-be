package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.PasswordResetToken;
import com.duszek.pindrop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByToken(String token);

	void deleteAllByUser(User user);
}
