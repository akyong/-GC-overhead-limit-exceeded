package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;
import java.util.Date;
@Singleton
public class BNIPreviousResponse {
    @JsonProperty("transactionStatus")
    private char transactionStatus;

    @JsonProperty("previousResponseCode")
    private String previousResponseCode;

    @JsonProperty("previousResponseMessage")
    private String previousResponseMessage;

    @JsonProperty("previousResponseTimestamp")
    private Date previousResponseTimestamp;

    @JsonProperty("debitAccountNo")
    private int debitAccountNo;

    @JsonProperty("creditAccountNo")
    private int creditAccountNo;

    @JsonProperty("valueAmount")
    private int valueAmount;

    @JsonProperty("valueCurrency")
    private String valueCurrency;

    public void setTransactionStatus(char transactionStatus) { this.transactionStatus = transactionStatus; }

    public char getTransactionStatus() { return transactionStatus; }

    public void setPreviousResponseCode(String previousResponseCode) { this.previousResponseCode = previousResponseCode; }

    public String getPreviousResponseCode() { return previousResponseCode; }

    public void setPreviousResponseMessage(String previousResponseMessage) { this.previousResponseMessage = previousResponseMessage; }

    public String getPreviousResponseMessage() { return previousResponseMessage; }

    public void setPreviousResponseTimestamp(Date previousResponseTimestamp) { this.previousResponseTimestamp = previousResponseTimestamp; }

    public Date getPreviousResponseTimestamp() { return previousResponseTimestamp; }

    public void setDebitAccountNo(int debitAccountNo) { this.debitAccountNo = debitAccountNo; }

    public int getDebitAccountNo() { return debitAccountNo; }

    public void setCreditAccountNo(int creditAccountNo) { this.creditAccountNo = creditAccountNo; }

    public int getCreditAccountNo() { return creditAccountNo; }

    public void setValueAmount(int valueAmount) { this.valueAmount = valueAmount; }

    public int getValueAmount() { return valueAmount; }

    public void setValueCurrency(String valueCurrency) { this.valueCurrency = valueCurrency; }

    public String getValueCurrency() { return valueCurrency; }

    @Override
    public String toString() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
