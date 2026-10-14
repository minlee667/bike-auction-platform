package com.example.bike_auction_platform.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bids",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bid_auction_request",
                columnNames = {"auction_id", "request_id"}
        ),
        indexes = @Index(
                name = "idx_bid_auction_created",
                columnList = "auction_id, created_at"
        )
)
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auction_id", nullable = false)
    private UUID auctionId;

    @Column(name = "bidder_id", nullable = false)
    private UUID bidderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Bid() {}

    public Bid(
            UUID auctionId,
            UUID bidderId,
            BigDecimal amount,
            UUID requestId,
            Instant createdAt
    ) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.requestId = requestId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAuctionId() { return auctionId; }
    public UUID getBidderId() { return bidderId; }
    public BigDecimal getAmount() { return amount; }
    public UUID getRequestId() { return requestId; }
    public Instant getCreatedAt() { return createdAt; }
}