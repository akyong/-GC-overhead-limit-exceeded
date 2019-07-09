package bank.transaction.service.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ReceivedPaymentVIrfan {

    @JsonProperty("invoice_numner")
    private List<String> invoiceNo;

    public void setInvoiceNo(List<String> invoiceNo) { this.invoiceNo = invoiceNo; }

    public List<String> getInvoiceNo() { return invoiceNo; }

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
