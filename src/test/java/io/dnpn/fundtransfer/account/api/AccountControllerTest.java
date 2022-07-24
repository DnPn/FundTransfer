package io.dnpn.fundtransfer.account.api;

import io.dnpn.fundtransfer.account.Account;
import io.dnpn.fundtransfer.account.AccountTestHelper;
import io.dnpn.fundtransfer.account.accessor.AccountAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountAccessor accessor;
    @InjectMocks
    private AccountController controller;

    @Test
    void GIVEN_accountExists_WHEN_getById_THEN_returnAccount() {
        doReturn(Optional.of(AccountTestHelper.ACCOUNT_A))
                .when(accessor)
                .getById(AccountTestHelper.ID_ACCOUNT_A);

        ResponseEntity<Account> response = controller.getById(AccountTestHelper.ID_ACCOUNT_A);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(AccountTestHelper.ACCOUNT_A, response.getBody());
    }

    @Test
    void GIVEN_accountNotFound_WHEN_getById_THEN_returnNotFound() {
        doReturn(Optional.empty())
                .when(accessor)
                .getById(AccountTestHelper.ID_ACCOUNT_A);

        ResponseEntity<Account> response = controller.getById(AccountTestHelper.ID_ACCOUNT_A);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * This condition is tested just for test coverage purpose. In practice the Pageable parameter will never be null
     * when making the GET call (Spring uses a default Pageable instance if needed).
     */
    @Test
    void GIVEN_nullPageable_WHEN_getAll_THEN_throwNullPointer() {
        assertThrows(NullPointerException.class, () -> controller.getAll(null));
    }

    @Test
    void WHEN_getAll_THEN_returnAccounts() {
        Pageable pageable = Pageable.unpaged();
        List<Account> accountList = List.of(AccountTestHelper.ACCOUNT_A, AccountTestHelper.ACCOUNT_B);
        Page<Account> accountPage = new PageImpl<>(accountList);
        doReturn(accountPage).when(accessor).list(pageable);

        ResponseEntity<Page<Account>> response = controller.getAll(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountPage, response.getBody());
    }

}