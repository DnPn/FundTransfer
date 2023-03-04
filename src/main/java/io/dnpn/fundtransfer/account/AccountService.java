package io.dnpn.fundtransfer.account;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;

    public Page<AccountEntity> list(@NonNull Pageable pageable) {
        log.trace("List the accounts with paging options: {}", pageable);
        return repository.findAll(pageable);
    }

    public Optional<AccountEntity> getById(long id) {
        log.trace("Get account {}", id);
        return repository.findById(id);
    }

    public void update(@NonNull AccountEntity account) {
        log.trace("Persist account data: {}", account);
        repository.save(account);
    }
}
