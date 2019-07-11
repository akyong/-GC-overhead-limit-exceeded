package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class BNIRequestPayload {
    @JsonProperty("cliendId")
    private String cliendId;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("customerReferenceNumber")
    private String customerReferenceNumber;

    public void setCliendId(String cliendId) {
        this.cliendId = cliendId;
    }

    public String getCliendId() {
        return cliendId;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void setCustomerReferenceNumber(String customerReferenceNumber) {
        this.customerReferenceNumber = customerReferenceNumber;
    }

    public String getCustomerReferenceNumber() {
        return customerReferenceNumber;
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
