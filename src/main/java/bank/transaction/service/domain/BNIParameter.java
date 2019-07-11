package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class BNIParameter {
    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMessage")
    private String responseMessage;

    @JsonProperty("responseTimestamp")
    private Date responseTimestamp;

    @JsonProperty("customerReference")
    private String customerReference;

    @JsonProperty("bankReference")
    private String bankReference;

    @JsonProperty("previousResponse")
    private BNIPreviousResponse bniPreviousResponse;

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseTimestamp(Date responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Date getResponseTimestamp() {
        return responseTimestamp;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getBankReference() {
        return bankReference;
    }

    public void setBankReference(String bankReference) {
        this.bankReference = bankReference;
    }

    public void setBniPreviousResponse(BNIPreviousResponse bniPreviousResponse) {
        this.bniPreviousResponse = bniPreviousResponse;
    }

    public BNIPreviousResponse getBniPreviousResponse() {
        return bniPreviousResponse;
    }

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
