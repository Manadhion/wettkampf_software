package io.github.manadhion.wettkampf.server.fachdaten;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

class SynchronisationsServiceTest {

    @Test
    void ersetztNurFachdatenUndLaesstKontoSowieSitzungenUnberuehrt() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), eq(4L))).thenReturn(1);
        SynchronisationsService service = new SynchronisationsService(jdbc);
        SynchronisationsSnapshot leer = new SynchronisationsSnapshot(4, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        long neueRevision = service.speichern(leer);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).execute(sql.capture());
        assertTrue(sql.getValue().contains("ergebnis"));
        assertTrue(sql.getValue().contains("saison"));
        assertFalse(sql.getValue().contains("konto"));
        assertFalse(sql.getValue().contains("sitzung"));
        assertTrue(neueRevision == 5);
    }

    @Test
    void lehntSnapshotMitVeralteterRevisionVorDemLoeschenAb() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        SynchronisationsService service = new SynchronisationsService(jdbc);
        SynchronisationsSnapshot leer = new SynchronisationsSnapshot(3, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        assertThrows(io.github.manadhion.wettkampf.server.web.VersionskonfliktException.class,
                () -> service.speichern(leer));
    }
}
