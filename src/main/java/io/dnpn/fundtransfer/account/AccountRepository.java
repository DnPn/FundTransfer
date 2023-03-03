package io.dnpn.fundtransfer.account;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnBean(AccountService.class)
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
}
