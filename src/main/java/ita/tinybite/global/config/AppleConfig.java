package ita.tinybite.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class AppleConfig {
    
    private static final String APPLE_JWK_URL = "https://appleid.apple.com/auth/keys";

    @Bean
    public JwtDecoder appleJwtDecoder() {
        return NimbusJwtDecoder
                .withJwkSetUri(APPLE_JWK_URL)
                .build();
    }
}
