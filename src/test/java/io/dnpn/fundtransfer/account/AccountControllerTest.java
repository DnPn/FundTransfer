package io.dnpn.fundtransfer.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static io.dnpn.fundtransfer.account.AccountTestHelper.ACCOUNT_A;
import static io.dnpn.fundtransfer.account.AccountTestHelper.ACCOUNT_B;
import static io.dnpn.fundtransfer.account.AccountTestHelper.ACCOUNT_ENTITY_A;
import static io.dnpn.fundtransfer.account.AccountTestHelper.ACCOUNT_ENTITY_B;
import static io.dnpn.fundtransfer.account.AccountTestHelper.ID_ACCOUNT_A;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService service;
    @InjectMocks
    private AccountController controller;

    @Test
    void GIVEN_accountExists_WHEN_getById_THEN_returnAccount() {
        doReturn(Optional.of(ACCOUNT_ENTITY_A))
                .when(service)
                .getById(ID_ACCOUNT_A);

        var response = controller.getById(ID_ACCOUNT_A);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(AccountTestHelper.ACCOUNT_A, response.getBody());
    }

    @Test
    void GIVEN_accountNotFound_WHEN_getById_THEN_returnNotFound() {
        doReturn(Optional.empty())
                .when(service)
                .getById(ID_ACCOUNT_A);

        ResponseEntity<Account> response = controller.getById(ID_ACCOUNT_A);

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
        var accountEntities = List.of(ACCOUNT_ENTITY_A, ACCOUNT_ENTITY_B);
        var accountEntitiesPage = new PageImpl<>(accountEntities);
        doReturn(accountEntitiesPage).when(service).list(pageable);

        var response = controller.getAll(pageable);

        var expectedAccountPage = new PageImpl<>(List.of(ACCOUNT_A, ACCOUNT_B));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAccountPage, response.getBody());
    }

}