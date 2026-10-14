package com.example.bike_auction_platform.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateAuctionRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal startingPrice,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal minimumIncrement,

        @NotNull
        Instant startsAt,

        @NotNull
        Instant endsAt
) {}