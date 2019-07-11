package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class BNIBodyRequestForPaymentStatus {

    public BNIBodyRequestForPaymentStatus(String clientId, String signature, String customerReferenceNumber){
        this.clientId = clientId;
        this.signature = signature;
        this.customerReferenceNumber = customerReferenceNumber;
    }

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("customerReferenceNumber")
    private String customerReferenceNumber;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
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
