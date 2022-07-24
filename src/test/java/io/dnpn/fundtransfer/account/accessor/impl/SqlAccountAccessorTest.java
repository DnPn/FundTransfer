package io.dnpn.fundtransfer.account.accessor.impl;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.AccountTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqlAccountAccessorTest {
    private static final JpaAccountEntity JPA_ACCOUNT_A = JpaAccountEntity.builder()
            .id(AccountTestHelper.ID_ACCOUNT_A)
            .balance(AccountTestHelper.BALANCE_ACCOUNT_A)
            .currency(AccountTestHelper.CURRENCY_ACCOUNT_A)
            .build();
    private static final JpaAccountEntity JPA_ACCOUNT_B = JpaAccountEntity.builder()
            .id(AccountTestHelper.ID_ACCOUNT_B)
            .balance(AccountTestHelper.BALANCE_ACCOUNT_B)
            .currency(AccountTestHelper.CURRENCY_ACCOUNT_B)
            .build();

    @Mock
    private JpaAccountAccessor jpaAccessor;
    @InjectMocks
    private SqlAccountAccessor sqlAccessor;

    @Test
    void WHEN_list_THEN_returnAccounts() {
        Pageable pageable = Pageable.unpaged();

        List<JpaAccountEntity> jpaList = List.of(JPA_ACCOUNT_A, JPA_ACCOUNT_B);
        Page<JpaAccountEntity> jpaPage = new PageImpl<>(jpaList);
        doReturn(jpaPage).when(jpaAccessor).findAll(pageable);

        Page<Account> actualPage = sqlAccessor.list(pageable);

        List<Account> expectedAccounts = List.of(AccountTestHelper.ACCOUNT_A, AccountTestHelper.ACCOUNT_B);
        Page<Account> expectedPage = new PageImpl<>(expectedAccounts);
        assertEquals(expectedPage, actualPage);
    }

    @Test
    void GIVEN_nullPageable_WHEN_list_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> sqlAccessor.list(null));
    }

    @Test
    void GIVEN_accountExists_WHEN_getById_THEN_returnAccount() {
        doReturn(Optional.of(JPA_ACCOUNT_A))
                .when(jpaAccessor)
                .findById(AccountTestHelper.ID_ACCOUNT_A);

        Optional<Account> actual = sqlAccessor.getById(AccountTestHelper.ID_ACCOUNT_A);

        Optional<Account> expected = Optional.of(AccountTestHelper.ACCOUNT_A);
        assertEquals(expected, actual);
    }

    @Test
    void GIVEN_accountNotFound_WHEN_getById_THEN_returnEmpty() {
        doReturn(Optional.empty())
                .when(jpaAccessor)
                .findById(AccountTestHelper.ID_ACCOUNT_A);

        Optional<Account> actual = sqlAccessor.getById(AccountTestHelper.ID_ACCOUNT_A);

        assertEquals(Optional.empty(), actual);
    }

    @Test
    void GIVEN_nullAccount_WHEN_update_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> sqlAccessor.update(null));
    }

    @Test
    void WHEN_update_THEN_callJpaSave() {
        sqlAccessor.update(AccountTestHelper.ACCOUNT_A);

        verify(jpaAccessor).save(JPA_ACCOUNT_A);
    }

}