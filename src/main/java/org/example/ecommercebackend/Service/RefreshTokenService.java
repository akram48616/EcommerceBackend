package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.Entity.RefreshToken;
import org.example.ecommercebackend.Entity.TokenBlacklist;
import org.example.ecommercebackend.Repository.RefreshTokenRepository;
import org.example.ecommercebackend.Repository.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository blacklistRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public RefreshToken saveRefreshToken(String username, String token) {
        log.info("Saving refresh token for username={}", username);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .username(username)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token saved for username={}, expiryDate={}", username, saved.getExpiryDate());

        return saved;
    }

    @Transactional(readOnly = true)
    public boolean validateRefreshToken(String token) {
        boolean valid = refreshTokenRepository.isTokenValid(token, LocalDateTime.now());
        log.debug("Refresh token validation result: {}", valid);
        if (!valid) {
            log.warn("Refresh token validation failed - token is invalid, expired, or revoked");
        }
        return valid;
    }

    @Transactional(readOnly = true)
    public String getUsernameFromToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUsername)
                .orElseThrow(() -> {
                    log.warn("Refresh token lookup failed - token not found");
                    return new RuntimeException("Refresh token not found");
                });
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresentOrElse(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revoked for username={}", rt.getUsername());
        }, () -> log.warn("Attempted to revoke a refresh token that was not found"));
    }

    @Transactional
    public void revokeAllUserTokens(String username) {
        log.info("Revoking all refresh tokens for username={}", username);
        refreshTokenRepository.revokeAllUserTokens(username);
    }

    @Transactional
    public void blacklistAccessToken(String token, String username, LocalDateTime expiresAt) {
        log.info("Blacklisting access token for username={}, expiresAt={}", username, expiresAt);

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .username(username)
                .blacklistedAt(LocalDateTime.now())
                .tokenExpiresAt(expiresAt)
                .build();

        blacklistRepository.save(blacklist);
    }

    @Transactional(readOnly = true)
    public boolean isAccessTokenBlacklisted(String token) {
        boolean blacklisted = blacklistRepository.existsByToken(token);
        if (blacklisted) {
            log.warn("Blocked request using blacklisted access token");
        }
        return blacklisted;
    }
}