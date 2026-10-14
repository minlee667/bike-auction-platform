package com.example.bike_auction_platform.repository;

import com.example.bike_auction_platform.model.Auction;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionRepository
        extends JpaRepository<Auction, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :auctionId")
    Optional<Auction> findByIdForUpdate(
            @Param("auctionId") UUID auctionId
    );

    List<Auction> findAllByOrderByEndsAtAsc();
}