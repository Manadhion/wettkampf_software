package io.github.manadhion.wettkampf.server.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TokenHashTest {

    private final TokenHash tokenHash = new TokenHash();

    @Test
    void erzeugtZufaelligeTokenUndStabileHashes() {
        String erstesToken = tokenHash.neuesToken();
        String zweitesToken = tokenHash.neuesToken();

        assertNotEquals(erstesToken, zweitesToken);
        assertEquals(tokenHash.hashen(erstesToken), tokenHash.hashen(erstesToken));
        assertEquals(64, tokenHash.hashen(erstesToken).length());
    }
}
