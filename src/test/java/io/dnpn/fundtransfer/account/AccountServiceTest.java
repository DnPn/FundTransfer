package io.dnpn.fundtransfer.account;

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
class AccountServiceTest {
    private static final AccountEntity JPA_ACCOUNT_A = AccountEntity.builder()
            .id(AccountTestHelper.ID_ACCOUNT_A)
            .balance(AccountTestHelper.BALANCE_ACCOUNT_A)
            .currency(AccountTestHelper.CURRENCY_ACCOUNT_A)
            .build();
    private static final AccountEntity JPA_ACCOUNT_B = AccountEntity.builder()
            .id(AccountTestHelper.ID_ACCOUNT_B)
            .balance(AccountTestHelper.BALANCE_ACCOUNT_B)
            .currency(AccountTestHelper.CURRENCY_ACCOUNT_B)
            .build();

    @Mock
    private AccountRepository repository;
    @InjectMocks
    private AccountService service;

    @Test
    void WHEN_list_THEN_returnAccounts() {
        Pageable pageable = Pageable.unpaged();

        List<AccountEntity> jpaList = List.of(JPA_ACCOUNT_A, JPA_ACCOUNT_B);
        Page<AccountEntity> jpaPage = new PageImpl<>(jpaList);
        doReturn(jpaPage).when(repository).findAll(pageable);

        Page<Account> actualPage = service.list(pageable);

        List<Account> expectedAccounts = List.of(AccountTestHelper.ACCOUNT_A, AccountTestHelper.ACCOUNT_B);
        Page<Account> expectedPage = new PageImpl<>(expectedAccounts);
        assertEquals(expectedPage, actualPage);
    }

    @Test
    void GIVEN_nullPageable_WHEN_list_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> service.list(null));
    }

    @Test
    void GIVEN_accountExists_WHEN_getById_THEN_returnAccount() {
        doReturn(Optional.of(JPA_ACCOUNT_A))
                .when(repository)
                .findById(AccountTestHelper.ID_ACCOUNT_A);

        Optional<Account> actual = service.getById(AccountTestHelper.ID_ACCOUNT_A);

        Optional<Account> expected = Optional.of(AccountTestHelper.ACCOUNT_A);
        assertEquals(expected, actual);
    }

    @Test
    void GIVEN_accountNotFound_WHEN_getById_THEN_returnEmpty() {
        doReturn(Optional.empty())
                .when(repository)
                .findById(AccountTestHelper.ID_ACCOUNT_A);

        Optional<Account> actual = service.getById(AccountTestHelper.ID_ACCOUNT_A);

        assertEquals(Optional.empty(), actual);
    }

    @Test
    void GIVEN_nullAccount_WHEN_update_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> service.update(null));
    }

    @Test
    void WHEN_update_THEN_callSaveOnUpdatedContact() {
        var accountABeforeUpdate = JPA_ACCOUNT_A.toBuilder()
                .balance(AccountTestHelper.BALANCE_ACCOUNT_B)
                .currency(AccountTestHelper.CURRENCY_ACCOUNT_B)
                .build();
        doReturn(Optional.of(accountABeforeUpdate))
                .when(repository)
                .findById(AccountTestHelper.ID_ACCOUNT_A);

        service.update(AccountTestHelper.ACCOUNT_A);

        verify(repository).save(JPA_ACCOUNT_A);
    }

}