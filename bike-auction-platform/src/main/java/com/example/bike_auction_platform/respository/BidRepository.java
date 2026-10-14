package com.example.bike_auction_platform.repository;

import com.example.bike_auction_platform.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByAuctionIdOrderByCreatedAtDesc(UUID auctionId);

    Optional<Bid> findByAuctionIdAndRequestId(
            UUID auctionId,
            UUID requestId
    );
}