
package com.example.bike_auction_platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final com.example.bike_auction_platform.security.JwtService jwtService;

    public JwtAuthenticationFilter(com.example.bike_auction_platform.security.JwtService jwtService) {
        this.jwtService = jwtService;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/auth/register")
                || path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null
                && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            String token = header.substring(7).trim();

            if (!token.isEmpty() && jwtService.isValid(token)) {
                try {
                    Claims claims = jwtService.parseClaims(token);

                    String userId = claims.getSubject();
                    String role = claims.get("role", String.class);

                    if (userId != null
                            && role != null
                            && (role.equals("USER") || role.equals("ADMIN"))) {

                        var authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        List.of(new SimpleGrantedAuthority(
                                                "ROLE_" + role))
                                );

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                } catch (RuntimeException exception) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
