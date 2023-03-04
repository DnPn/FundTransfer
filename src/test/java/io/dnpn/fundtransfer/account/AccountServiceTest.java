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

import static io.dnpn.fundtransfer.account.AccountTestHelper.BALANCE_ACCOUNT_A;
import static io.dnpn.fundtransfer.account.AccountTestHelper.BALANCE_ACCOUNT_B;
import static io.dnpn.fundtransfer.account.AccountTestHelper.CURRENCY_ACCOUNT_A;
import static io.dnpn.fundtransfer.account.AccountTestHelper.CURRENCY_ACCOUNT_B;
import static io.dnpn.fundtransfer.account.AccountTestHelper.ID_ACCOUNT_A;
import static io.dnpn.fundtransfer.account.AccountTestHelper.ID_ACCOUNT_B;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final AccountEntity ACCOUNT_A = AccountEntity.builder()
            .id(ID_ACCOUNT_A)
            .balance(BALANCE_ACCOUNT_A)
            .currency(CURRENCY_ACCOUNT_A)
            .build();
    private static final AccountEntity ACCOUNT_B = AccountEntity.builder()
            .id(ID_ACCOUNT_B)
            .balance(BALANCE_ACCOUNT_B)
            .currency(CURRENCY_ACCOUNT_B)
            .build();

    @Mock
    private AccountRepository repository;
    @InjectMocks
    private AccountService service;

    @Test
    void WHEN_list_THEN_returnAccounts() {
        Pageable pageable = Pageable.unpaged();

        List<AccountEntity> jpaList = List.of(ACCOUNT_A, ACCOUNT_B);
        Page<AccountEntity> jpaPage = new PageImpl<>(jpaList);
        doReturn(jpaPage).when(repository).findAll(pageable);

        var actualPage = service.list(pageable);

        var expectedAccounts = List.of(ACCOUNT_A, ACCOUNT_B);
        var expectedPage = new PageImpl<>(expectedAccounts);
        assertEquals(expectedPage, actualPage);
    }

    @Test
    void GIVEN_nullPageable_WHEN_list_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> service.list(null));
    }

    @Test
    void GIVEN_accountExists_WHEN_getById_THEN_returnAccount() {
        doReturn(Optional.of(ACCOUNT_A))
                .when(repository)
                .findById(ACCOUNT_A.getId());

        var actual = service.getById(ACCOUNT_A.getId());

        var expected = Optional.of(ACCOUNT_A);
        assertEquals(expected, actual);
    }

    @Test
    void GIVEN_accountNotFound_WHEN_getById_THEN_returnEmpty() {
        doReturn(Optional.empty())
                .when(repository)
                .findById(ACCOUNT_A.getId());

        var actual = service.getById(ACCOUNT_A.getId());

        assertEquals(Optional.empty(), actual);
    }

    @Test
    void GIVEN_nullAccount_WHEN_update_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> service.update(null));
    }

    @Test
    void WHEN_update_THEN_callSaveOnUpdatedContact() {
        service.update(ACCOUNT_A);

        verify(repository).save(ACCOUNT_A);
    }

}