package io.dnpn.fundtransfer.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/ping")
    ResponseEntity ping() {
        return ResponseEntity.noContent().build();
    }
}