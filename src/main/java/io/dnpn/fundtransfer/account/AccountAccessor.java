package io.dnpn.fundtransfer.account;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Accessor for accounts.
 */
public interface AccountAccessor {

    /**
     * List the available accounts.
     *
     * @param pageable paging options.
     * @return the accounts matching the paging options.
     */
    Page<Account> list(@NonNull Pageable pageable);

    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id the unique identifier of the account.
     * @return the account.
     */
    Optional<Account> getById(long id);

    /**
     * Updates the given account. If there is already an account in the persistence layer with the same identifier
     * then it is updated, if no account has the same identifier then a new account is created.
     *
     * @param account the account details.
     */
    void update(@NonNull Account account);
}
