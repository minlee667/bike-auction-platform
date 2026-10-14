package com.example.bike_auction_platform.dto;

import com.example.bike_auction_platform.model.Bid;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BidResponse(
        UUID id,
        UUID auctionId,
        UUID bidderId,
        BigDecimal amount,
        UUID requestId,
        Instant createdAt
) {
    public static BidResponse from(Bid bid) {
        return new BidResponse(
                bid.getId(),
                bid.getAuctionId(),
                bid.getBidderId(),
                bid.getAmount(),
                bid.getRequestId(),
                bid.getCreatedAt()
        );
    }
}