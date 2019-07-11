package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class InvoiceIdAndResellerCode {

    public InvoiceIdAndResellerCode(int invoiceId, int resellerCode){
        this.invoiceId = invoiceId;
        this.resellerCode = resellerCode;
    }

    @JsonProperty("invoice_id")
    private int invoiceId;

    @JsonProperty("reseller_code")
    private int resellerCode;

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setResellerCode(int resellerCode) {
        this.resellerCode = resellerCode;
    }

    public int getResellerCode() {
        return resellerCode;
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
