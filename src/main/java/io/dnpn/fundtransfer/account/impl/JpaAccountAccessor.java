package io.dnpn.fundtransfer.account.impl;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaAccountAccessor extends JpaRepository<JpaAccountEntity, Long> {
}
