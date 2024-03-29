package io.dnpn.fundtransfer.currency.service.impl.sql;

import io.dnpn.fundtransfer.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "exchange_rate")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
class ExchangeRateEntity {
    @Id
    private Currency currency;
    @Version
    private Integer version;
    @Column(precision = 20, scale = 10)
    private BigDecimal rateToUsd;
}
