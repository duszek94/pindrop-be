package com.duszek.pindrop.repository;

import com.duszek.pindrop.entity.TripProposal;
import com.duszek.pindrop.entity.ProposalType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripProposalRepository extends JpaRepository<TripProposal, Long> {

	List<TripProposal> findByTripIdOrderByProposalTypeAsc(Long tripId);

	Optional<TripProposal> findByTripIdAndProposalType(Long tripId, ProposalType proposalType);

	void deleteByTripId(Long tripId);
}
