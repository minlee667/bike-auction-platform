
package com.example.bike_auction_platform.service;

import com.example.bike_auction_platform.dto.AuctionResponse;
import com.example.bike_auction_platform.dto.CreateAuctionRequest;
import com.example.bike_auction_platform.exception.AuctionNotFoundException;
import com.example.bike_auction_platform.model.Auction;
import com.example.bike_auction_platform.repository.AuctionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Transactional
    public AuctionResponse createAuction(
            CreateAuctionRequest request,
            UUID sellerId) {

        Instant now = Instant.now();

        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException(
                    "Auction end time must be after start time");
        }

        if (!request.endsAt().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Auction end time must be in the future");
        }

        Auction auction = new Auction(
                sellerId,
                request.title(),
                request.startingPrice(),
                request.minimumIncrement(),
                request.startsAt(),
                request.endsAt()
        );

        return AuctionResponse.from(auctionRepository.save(auction));
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAllByOrderByEndsAtAsc()
                .stream()
                .map(AuctionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuctionResponse getAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(
                        () -> new AuctionNotFoundException(auctionId));

        return AuctionResponse.from(auction);
    }

    @Transactional
    public AuctionResponse startAuction(
            UUID auctionId,
            UUID userId,
            boolean isAdmin) {

        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(
                        () -> new AuctionNotFoundException(auctionId));

        verifyOwner(auction, userId, isAdmin);
        auction.start(Instant.now());

        return AuctionResponse.from(auction);
    }

    @Transactional
    public AuctionResponse closeAuction(
            UUID auctionId,
            UUID userId,
            boolean isAdmin) {

        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(
                        () -> new AuctionNotFoundException(auctionId));

        verifyOwner(auction, userId, isAdmin);
        auction.close(Instant.now());

        return AuctionResponse.from(auction);
    }

    private void verifyOwner(
            Auction auction,
            UUID userId,
            boolean isAdmin) {

        if (isAdmin) {
            return;
        }

        if (auction.getSellerId() == null
                || !auction.getSellerId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to manage this auction");
        }
    }
}
