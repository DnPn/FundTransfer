package io.dnpn.fundtransfer.account.accessor.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnBean(SqlAccountAccessor.class)
public interface JpaAccountAccessor extends JpaRepository<JpaAccountEntity, Long> {
}
