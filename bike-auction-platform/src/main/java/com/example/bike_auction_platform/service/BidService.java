
package com.example.bike_auction_platform.service;

import com.example.bike_auction_platform.dto.BidResponse;
import com.example.bike_auction_platform.dto.PlaceBidRequest;
import com.example.bike_auction_platform.exception.AuctionNotFoundException;
import com.example.bike_auction_platform.exception.InvalidBidException;
import com.example.bike_auction_platform.model.Auction;
import com.example.bike_auction_platform.model.Bid;
import com.example.bike_auction_platform.repository.AuctionRepository;
import com.example.bike_auction_platform.repository.BidRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public BidService(
            AuctionRepository auctionRepository,
            BidRepository bidRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
    }

    @Transactional
    public BidResponse placeBid(
            UUID auctionId,
            UUID bidderId,
            PlaceBidRequest request) {

        Auction auction = auctionRepository
                .findByIdForUpdate(auctionId)
                .orElseThrow(
                        () -> new AuctionNotFoundException(auctionId));

        var existingBid = bidRepository.findByAuctionIdAndRequestId(
                auctionId,
                request.requestId()
        );

        if (existingBid.isPresent()) {
            Bid previousBid = existingBid.get();

            boolean sameBidder =
                    previousBid.getBidderId().equals(bidderId);

            boolean sameAmount =
                    previousBid.getAmount().compareTo(request.amount()) == 0;

            if (!sameBidder || !sameAmount) {
                throw new InvalidBidException(
                        "Request ID has already been used for a different bid");
            }

            return BidResponse.from(previousBid);
        }

        Instant now = Instant.now();

        try {
            auction.placeBid(request.amount(), now);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new InvalidBidException(exception.getMessage());
        }

        Bid bid = new Bid(
                auctionId,
                bidderId,
                request.amount(),
                request.requestId(),
                now
        );

        bidRepository.save(bid);
        auctionRepository.save(auction);

        return BidResponse.from(bid);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> getBidHistory(UUID auctionId) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new AuctionNotFoundException(auctionId);
        }

        return bidRepository
                .findByAuctionIdOrderByCreatedAtDesc(auctionId)
                .stream()
                .map(BidResponse::from)
                .toList();
    }
}
