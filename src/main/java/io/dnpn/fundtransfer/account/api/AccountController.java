package io.dnpn.fundtransfer.account.api;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
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
    @GetMapping("/accounts")
    public ResponseEntity<Page<Account>> getAll(@NonNull Pageable pageable) {
        return ResponseEntity.ok(accessor.list(pageable));
    }
}
