package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class TransactionResponse {
    @JsonProperty("TransactionID")
    private String transactionId;

    public String getTransactionId() {
        return transactionId;
    }
}
