package io.dnpn.fundtransfer.transfer.api;

/**
 * Field labels for {@link TransferApiRequest} and {@link TransferApiResponse}.
 */
public final class TransferApiField {

    public static final String FROM_ACCOUNT = "fromAccount";
    public static final String TO_ACCOUNT = "toAccount";
    public static final String AMOUNT = "amount";
    public static final String REQUEST = "request";
    public static final String TIMESTAMP = "timestamp";
    public static final String MESSAGE = "message";

    private TransferApiField() {
        
    }
}
