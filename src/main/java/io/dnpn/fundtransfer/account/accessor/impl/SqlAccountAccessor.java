package io.dnpn.fundtransfer.account.accessor.impl;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlAccountAccessor implements AccountAccessor {

    private final JpaAccountAccessor jpaAccessor;

    private JpaAccountEntity toJpaEntity(Account account) {
        return JpaAccountEntity.builder()
                .id(account.getAccountId())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .build();
    }

    @Override
    public Page<Account> list(@NonNull Pageable pageable) {
        return jpaAccessor.findAll(pageable)
                .map(this::toGenericEntity);
    }

    private Account toGenericEntity(JpaAccountEntity jpaEntity) {
        return Account.builder()
                .accountId(jpaEntity.getId())
                .currency(jpaEntity.getCurrency())
                .balance(jpaEntity.getBalance())
                .build();
    }

    @Override
    public Optional<Account> getById(long id) {
        return jpaAccessor.findById(id)
                .map(this::toGenericEntity);
    }

    @Override
    public void update(@NonNull Account account) {
        jpaAccessor.save(toJpaEntity(account));
    }
}
