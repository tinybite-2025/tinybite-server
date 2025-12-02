package ita.growin.domain.auth.entity;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import ita.growin.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    // Access Token 생성
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .claim("type", user.getType().name())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 검증
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // User ID 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    // 만료 시간 추출
    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }
}
