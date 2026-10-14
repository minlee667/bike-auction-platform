package com.example.bike_auction_platform.controller;

import com.example.bike_auction_platform.dto.AuctionResponse;
import com.example.bike_auction_platform.dto.BidResponse;
import com.example.bike_auction_platform.dto.CreateAuctionRequest;
import com.example.bike_auction_platform.service.AuctionService;
import com.example.bike_auction_platform.service.BidService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final BidService bidService;

    public AuctionController(
            AuctionService auctionService,
            BidService bidService) {
        this.auctionService = auctionService;
        this.bidService = bidService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionResponse createAuction(
            @Valid @RequestBody CreateAuctionRequest request,
            Authentication authentication) {

        UUID sellerId = authenticatedUserId(authentication);

        return auctionService.createAuction(request, sellerId);
    }

    @GetMapping
    public List<AuctionResponse> getAllAuctions() {
        return auctionService.getAllAuctions();
    }

    @GetMapping("/{auctionId}")
    public AuctionResponse getAuction(
            @PathVariable UUID auctionId) {
        return auctionService.getAuction(auctionId);
    }

    @PostMapping("/{auctionId}/start")
    public AuctionResponse startAuction(
            @PathVariable UUID auctionId,
            Authentication authentication) {

        return auctionService.startAuction(
                auctionId,
                authenticatedUserId(authentication),
                isAdmin(authentication)
        );
    }

    @PostMapping("/{auctionId}/close")
    public AuctionResponse closeAuction(
            @PathVariable UUID auctionId,
            Authentication authentication) {

        return auctionService.closeAuction(
                auctionId,
                authenticatedUserId(authentication),
                isAdmin(authentication)
        );
    }

    @GetMapping("/{auctionId}/bids")
    public List<BidResponse> getBidHistory(
            @PathVariable UUID auctionId) {
        return bidService.getBidHistory(auctionId);
    }

    private UUID authenticatedUserId(
            Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private boolean isAdmin(
            Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }
}
