package io.dnpn.fundtransfer.account;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;

    public Page<Account> list(@NonNull Pageable pageable) {
        log.trace("List the accounts with paging options: {}", pageable);
        return repository.findAll(pageable)
                .map(AccountService::toAccount);
    }

    public Optional<Account> getById(long id) {
        log.trace("Get account {}", id);
        return repository.findById(id)
                .map(AccountService::toAccount);
    }

    public void update(@NonNull Account account) {
        log.trace("Persist account data: {}", account);
        var accountEntity = repository.findById(account.accountId)
                .map(entity -> updateEntityWithDto(entity, account))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "No account with ID " + account.accountId));
        repository.save(accountEntity);
    }

    private static AccountEntity updateEntityWithDto(AccountEntity entity, Account dto) {
        return entity.toBuilder()
                .balance(dto.balance)
                .currency(dto.currency)
                .build();
    }

    private static Account toAccount(AccountEntity entity) {
        return Account.builder()
                .accountId(entity.getId())
                .currency(entity.getCurrency())
                .balance(entity.getBalance())
                .build();
    }
}
