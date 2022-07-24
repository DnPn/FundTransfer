package io.dnpn.fundtransfer.transfer.service;

/**
 * Exception used when the requested transfer is illegal (non-existing account or not enough money on the debit
 * account).
 */
public class IllegalTransferException extends Exception {

    public IllegalTransferException(String message) {
        super(message);
    }
}
