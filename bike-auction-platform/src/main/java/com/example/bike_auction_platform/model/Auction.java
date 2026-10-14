
package com.example.bike_auction_platform.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "seller_id")
    private UUID sellerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal startingPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumIncrement;

    @Column(nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuctionStatus status;

    @Version
    private Long version;

    protected Auction() {
    }

    public Auction(
            UUID sellerId,
            String title,
            BigDecimal startingPrice,
            BigDecimal minimumIncrement,
            Instant startsAt,
            Instant endsAt
    ) {
        this.sellerId = sellerId;
        this.title = title;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.minimumIncrement = minimumIncrement;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = AuctionStatus.SCHEDULED;
    }

    /**
     * Returns the status that should be displayed at the supplied time.
     * Expiration is derived from the end time, even if the database
     * still stores SCHEDULED or LIVE.
     */
    public AuctionStatus getEffectiveStatus(Instant now) {
        if ((status == AuctionStatus.SCHEDULED
                || status == AuctionStatus.LIVE)
                && !now.isBefore(endsAt)) {
            return AuctionStatus.EXPIRED;
        }

        return status;
    }

    /**
     * Starts an auction only when it is scheduled and its time window
     * has not expired.
     */
    public void start(Instant now) {
        if (status != AuctionStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Only scheduled auctions can start"
            );
        }

        if (!now.isBefore(endsAt)) {
            status = AuctionStatus.EXPIRED;

            throw new IllegalStateException(
                    "Auction has already ended"
            );
        }

        if (now.isBefore(startsAt)) {
            throw new IllegalStateException(
                    "Auction start time has not arrived"
            );
        }

        status = AuctionStatus.LIVE;
    }

    /**
     * Closes an auction that is currently live and has not expired.
     */
    public void close(Instant now) {
        if (status != AuctionStatus.LIVE) {
            throw new IllegalStateException(
                    "Only live auctions can close"
            );
        }

        if (!now.isBefore(endsAt)) {
            status = AuctionStatus.EXPIRED;

            throw new IllegalStateException(
                    "Auction has already ended"
            );
        }

        status = AuctionStatus.CLOSED;
    }

    /**
     * Records a bid only while the auction is live and before its
     * end time. The bid must meet the minimum increment.
     */
    public void placeBid(BigDecimal amount, Instant now) {
        if (status != AuctionStatus.LIVE) {
            throw new IllegalStateException(
                    "Auction is not live"
            );
        }

        if (!now.isBefore(endsAt)) {
            status = AuctionStatus.EXPIRED;

            throw new IllegalStateException(
                    "Auction has ended"
            );
        }

        BigDecimal minimumBid =
                currentPrice.add(minimumIncrement);

        if (amount == null || amount.compareTo(minimumBid) < 0) {
            throw new IllegalArgumentException(
                    "Bid must be at least " + minimumBid
            );
        }

        currentPrice = amount;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getMinimumIncrement() {
        return minimumIncrement;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    /**
     * Returns the effective status for callers, including API mapping.
     * The persisted status remains available internally to JPA.
     */
    public AuctionStatus getStatus() {
        return getEffectiveStatus(Instant.now());
    }

    public Long getVersion() {
        return version;
    }
}
