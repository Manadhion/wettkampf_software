package io.github.manadhion.wettkampf.server.auth;

import java.util.Optional;

final class BearerToken {

    private static final String PRAEFIX = "Bearer ";

    private BearerToken() {
    }

    static Optional<String> ausAuthorizationOptional(String authorization) {
        if (authorization == null || !authorization.startsWith(PRAEFIX)) {
            return Optional.empty();
        }
        String token = authorization.substring(PRAEFIX.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    static String ausAuthorization(String authorization) {
        return ausAuthorizationOptional(authorization)
                .orElseThrow(AnmeldungFehlgeschlagenException::new);
    }
}
