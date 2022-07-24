package io.dnpn.fundtransfer.account.accessor.impl;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SqlAccountAccessor implements AccountAccessor {

    private final JpaAccountAccessor jpaAccessor;

    @Override
    public Page<Account> list(@NonNull Pageable pageable) {
        log.debug("List the accounts with paging options: {}", pageable);
        return jpaAccessor.findAll(pageable)
                .map(this::toGenericEntity);
    }

    @Override
    public Optional<Account> getById(long id) {
        log.debug("Get account {}", id);
        return jpaAccessor.findById(id)
                .map(this::toGenericEntity);
    }

    @Override
    public void update(@NonNull Account account) {
        log.debug("Persist account data: {}", account);
        jpaAccessor.save(toJpaEntity(account));
    }

    private JpaAccountEntity toJpaEntity(Account account) {
        return JpaAccountEntity.builder()
                .id(account.getAccountId())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .build();
    }

    private Account toGenericEntity(JpaAccountEntity jpaEntity) {
        return Account.builder()
                .accountId(jpaEntity.getId())
                .currency(jpaEntity.getCurrency())
                .balance(jpaEntity.getBalance())
                .build();
    }
}
