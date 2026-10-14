
package com.example.bike_auction_platform.controller;

import com.example.bike_auction_platform.dto.BidResponse;
import com.example.bike_auction_platform.dto.PlaceBidRequest;
import com.example.bike_auction_platform.service.BidService;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public BidResponse placeBid(
            @PathVariable UUID auctionId,
            @Valid @RequestBody PlaceBidRequest request,
            Authentication authentication) {

        UUID bidderId = UUID.fromString(authentication.getName());

        return bidService.placeBid(
                auctionId,
                bidderId,
                request
        );
    }
}
