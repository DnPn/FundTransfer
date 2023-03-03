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

    private final AccountRepository jpaAccessor;

    public Page<Account> list(@NonNull Pageable pageable) {
        log.trace("List the accounts with paging options: {}", pageable);
        return jpaAccessor.findAll(pageable)
                .map(this::toGenericEntity);
    }

    public Optional<Account> getById(long id) {
        log.trace("Get account {}", id);
        return jpaAccessor.findById(id)
                .map(this::toGenericEntity);
    }

    public void update(@NonNull Account account) {
        log.trace("Persist account data: {}", account);
        jpaAccessor.save(toJpaEntity(account));
    }

    private AccountEntity toJpaEntity(Account account) {
        return AccountEntity.builder()
                .id(account.getAccountId())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .build();
    }

    private Account toGenericEntity(AccountEntity jpaEntity) {
        return Account.builder()
                .accountId(jpaEntity.getId())
                .currency(jpaEntity.getCurrency())
                .balance(jpaEntity.getBalance())
                .build();
    }
}
