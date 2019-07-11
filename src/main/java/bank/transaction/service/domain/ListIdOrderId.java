package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListIdOrderId {
    @JsonProperty("order_id")
    private List<Integer> orderId;

    public void setOrderId(List<Integer> orderId) {
        this.orderId = orderId;
    }

    public List<Integer> getOrderId() {
        return orderId;
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
