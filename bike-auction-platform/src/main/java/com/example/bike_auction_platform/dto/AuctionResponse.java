
package com.example.bike_auction_platform.dto;

import com.example.bike_auction_platform.model.Auction;
import com.example.bike_auction_platform.model.AuctionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AuctionResponse(
        UUID id,
        String title,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        BigDecimal minimumIncrement,
        Instant startsAt,
        Instant endsAt,
        AuctionStatus status,
        Long version
) {

    public static AuctionResponse from(Auction auction) {
        Instant now = Instant.now();

        AuctionStatus effectiveStatus =
                auction.getEffectiveStatus(now);

        return new AuctionResponse(
                auction.getId(),
                auction.getTitle(),
                auction.getStartingPrice(),
                auction.getCurrentPrice(),
                auction.getMinimumIncrement(),
                auction.getStartsAt(),
                auction.getEndsAt(),
                effectiveStatus,
                auction.getVersion()
        );
    }
}
