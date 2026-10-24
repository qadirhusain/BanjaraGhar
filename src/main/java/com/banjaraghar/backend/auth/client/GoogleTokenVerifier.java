package com.banjaraghar.backend.auth.client;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class GoogleTokenVerifier {

    private static final String JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    // Checks the signature against Google's public keys (cached) and the expiry time
    private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(JWKS_URI).build();
    private final List<String> clientIds;

    public GoogleTokenVerifier(AppProperties appProperties) {
        this.clientIds = appProperties.getGoogle().getClientIds();
    }

    public SocialProfile verify(String idToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid Google token");
        }

        boolean issuerOk = ISSUERS.contains(jwt.getClaimAsString("iss"));
        boolean audienceOk = jwt.getAudience() != null && jwt.getAudience().stream().anyMatch(clientIds::contains);
        if (!issuerOk || !audienceOk) {
            throw new UnauthorizedException("Invalid Google token");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || !Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            throw new BadRequestException("Your Google email address is not verified");
        }

        return new SocialProfile(jwt.getSubject(), email.trim().toLowerCase(), jwt.getClaimAsString("name"));
    }
}
