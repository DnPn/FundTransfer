package io.dnpn.fundtransfer.transfer.service;

/**
 * Exception used when the transfer requested is valid but the operation fails. It can be caused by a failure to
 * perform currency exchange or to update the balance of accounts.
 */
public class TransferFailureException extends Exception {

    public TransferFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
