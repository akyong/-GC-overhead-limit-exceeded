package bank.transaction.service.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ReceivePaymentValidation {

    @JsonProperty("invoice")
    private List<Integer> invoiceId;

    public void setInvoiceId(List<Integer> invoiceId) {
        this.invoiceId = invoiceId;
    }

    public List<Integer> getInvoiceId() {
        return invoiceId;
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
