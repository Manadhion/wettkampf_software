package io.github.manadhion.wettkampf.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.github.manadhion.wettkampf.data.Saison;

class ControllerDatenServiceTest {

    @Test
    void liestSaisonsAusschliesslichUeberDenEingesetztenDatenservice() {
        Saison erwartet = new Saison("online-saison", 2627);
        AtomicReference<String> aufgerufeneMethode = new AtomicReference<>();
        WettkampfDatenService service = (WettkampfDatenService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { WettkampfDatenService.class },
                (proxy, methode, argumente) -> {
                    aufgerufeneMethode.set(methode.getName());
                    return List.of(erwartet);
                });

        List<Saison> ergebnis = new Controller(service).alleSaisons();

        assertEquals("alleSaisons", aufgerufeneMethode.get());
        assertSame(erwartet, ergebnis.getFirst());
    }
}
