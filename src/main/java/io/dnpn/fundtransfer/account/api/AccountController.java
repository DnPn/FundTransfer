package io.dnpn.fundtransfer.account.api;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller exposing the available accounts.
 */
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountAccessor accessor;

    /**
     * Gets an account by its unique identifier.
     *
     * @param id the unique identifier.
     * @return the account.
     */
    @Operation(description = "Gets an account by its ID.", responses =
            {
                    @ApiResponse(responseCode = "200", description = "Found the account.",
                            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Account.class))}),
                    @ApiResponse(responseCode = "404", description = "Account not found.", content = @Content)
            })
    @GetMapping("/account/{id}")
    public ResponseEntity<Account> getById(@PathVariable long id) {
        final Optional<Account> account = accessor.getById(id);
        return ResponseEntity.of(account);
    }

    /**
     * List the available accounts.
     *
     * @param pageable the paging options.
     * @return the accounts matching the paging options.
     */
    @Operation(description = "Lists all accounts.", parameters = {
            @Parameter(name = "size", description = "number of items per page", example = "5", in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer($int32)", minimum = "0", exclusiveMinimum = true)),
            @Parameter(name = "page", description = "the page to get", example = "0", in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer($int32)", minimum = "0", exclusiveMinimum = false))
    })
    @GetMapping("/accounts")
    public ResponseEntity<Page<Account>> getAll(@Parameter(hidden = true) @NonNull Pageable pageable) {
        return ResponseEntity.ok(accessor.list(pageable));
    }
}
