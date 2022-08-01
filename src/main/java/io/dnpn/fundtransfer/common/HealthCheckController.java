package io.dnpn.fundtransfer.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @Operation(description = "Performs a shallow health check of the service.", responses =
            {@ApiResponse(responseCode = "204", description = "The service is running.", content = @Content)})
    @GetMapping("/ping")
    ResponseEntity<Void> ping() {
        return ResponseEntity.noContent().build();
    }
}
