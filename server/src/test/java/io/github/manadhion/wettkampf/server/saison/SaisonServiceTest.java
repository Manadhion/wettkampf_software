package io.github.manadhion.wettkampf.server.saison;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.manadhion.wettkampf.server.web.NichtGefundenException;
import io.github.manadhion.wettkampf.server.web.VersionskonfliktException;

class SaisonServiceTest {

    private final SaisonRepository repository = mock(SaisonRepository.class);
    private final SaisonService service = new SaisonService(repository);

    @Test
    void legtEineSaisonUeberDasRepositoryAn() {
        Saison erwartet = new Saison(UUID.randomUUID(), 2627, 0);
        when(repository.anlegen(2627)).thenReturn(erwartet);

        Saison saison = service.anlegen(2627);

        assertEquals(erwartet, saison);
        verify(repository).anlegen(2627);
    }

    @Test
    void erkenntEineZwischenzeitlichGeaenderteSaison() {
        UUID id = UUID.randomUUID();
        when(repository.aktualisieren(id, 2728, 3)).thenReturn(Optional.empty());
        when(repository.existiert(id)).thenReturn(true);

        assertThrows(VersionskonfliktException.class,
                () -> service.aktualisieren(id, 2728, 3));
    }

    @Test
    void unterscheidetFehlendeSaisonVomVersionskonflikt() {
        UUID id = UUID.randomUUID();
        when(repository.aktualisieren(id, 2728, 0)).thenReturn(Optional.empty());
        when(repository.existiert(id)).thenReturn(false);

        assertThrows(NichtGefundenException.class,
                () -> service.aktualisieren(id, 2728, 0));
    }
}
