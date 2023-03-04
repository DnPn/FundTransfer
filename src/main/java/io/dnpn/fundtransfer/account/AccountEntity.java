package io.dnpn.fundtransfer.account;

import io.dnpn.fundtransfer.common.MoneyHandling;
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
@Table(name = "account")
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    private long id;
    @Version
    private Integer version;
    private Currency currency;
    @Column(precision = MoneyHandling.PRECISION_FOR_MONEY, scale = MoneyHandling.SCALE_FOR_MONEY)
    private BigDecimal balance;

    public Account toDto() {
        return Account.builder()
                .accountId(this.id)
                .currency(this.currency)
                .balance(this.balance)
                .build();
    }
}
