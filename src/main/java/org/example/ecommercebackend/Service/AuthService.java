package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.*;
import org.example.ecommercebackend.DTO.ResponseDTO.*;
import org.example.ecommercebackend.Security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDTO login(LoginRequestDTO dto) {

        log.info("Login attempt for email={}", dto.getEmail());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        log.info("Authentication successful for email={}", dto.getEmail());

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        refreshTokenService.saveRefreshToken(
                userDetails.getUsername(),
                refreshToken
        );

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_CUSTOMER");

        log.info(
                "Login successful for email={}, role={}",
                userDetails.getUsername(),
                role
        );

        return LoginResponseDTO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .email(userDetails.getUsername())
                .role(role)
                .accessTokenExpiresIn(jwtUtil.getAccessExpiration())
                .refreshTokenExpiresIn(jwtUtil.getRefreshExpiration())
                .build();
    }

    public RefreshResponseDTO refresh(RefreshRequestDTO dto) {

        log.info("Refresh token request received");

        String refreshToken = dto.getRefreshToken();

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {

            log.warn("Invalid or expired refresh token received");

            throw new org.example.ecommercebackend.Exception.BadRequestException(
                    "Invalid or expired refresh token. Please login again."
            );
        }

        String username = refreshTokenService.getUsernameFromToken(refreshToken);

        log.info("Refresh token validated successfully for username={}", username);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        String newAccessToken =
                jwtUtil.generateAccessToken(userDetails);

        String newRefreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        refreshTokenService.revokeRefreshToken(refreshToken);

        refreshTokenService.saveRefreshToken(
                username,
                newRefreshToken
        );

        log.info(
                "Access token and refresh token successfully rotated for username={}",
                username
        );

        return RefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .accessTokenExpiresIn(jwtUtil.getAccessExpiration())
                .build();
    }

    public void logout(String accessToken, LogoutRequestDTO dto) {

        log.info("Logout request received");

        String username = jwtUtil.extractUsername(accessToken);

        java.util.Date expiry =
                jwtUtil.extractExpiration(accessToken);

        LocalDateTime expiresAt =
                expiry.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        refreshTokenService.blacklistAccessToken(
                accessToken,
                username,
                expiresAt
        );

        refreshTokenService.revokeAllUserTokens(username);

        log.info(
                "Logout successful for username={}, access token expiry={}",
                username,
                expiresAt
        );
    }
}