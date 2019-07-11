package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListIdInvoiceId {
    @JsonProperty("invoice_id")
    private List<Integer> invoiceId;

    public void setInvoiceId(List<Integer> orderId) {
        this.invoiceId = orderId;
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
