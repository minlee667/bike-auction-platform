
package com.example.bike_auction_platform.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UUID id,
        String displayName,
        String email,
        String role
) {}
