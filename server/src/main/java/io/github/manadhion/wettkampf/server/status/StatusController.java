package io.github.manadhion.wettkampf.server.status;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    @GetMapping
    public Map<String, String> status() {
        return Map.of("status", "bereit", "apiVersion", "v1");
    }
}
