package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class BNISignaturePayload {
    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("customerReferenceNumber")
    private String customerReferenceNumber;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
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

    public String JSONWithOutSpace(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"clientId\":\""+clientId+"\"");
        sb.append(",\"customerReferenceNumber\":\""+customerReferenceNumber+"\"");
        sb.append("}");

        return sb.toString();
    }
}
