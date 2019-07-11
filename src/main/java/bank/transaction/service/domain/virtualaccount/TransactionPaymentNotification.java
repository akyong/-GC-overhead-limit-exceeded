package bank.transaction.service.domain.virtualaccount;


import javax.persistence.*;

@Entity
@Table(name = "bni_callback")
public class TransactionPaymentNotification {

    public TransactionPaymentNotification(){}

    public TransactionPaymentNotification(String decode){

        this.decode = decode;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bni", unique = true)
    private Long id;

    @Column(name = "desc_callback", nullable = false)
    private String decode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDecode() {
        return decode;
    }

    public void setDecode(String decode) {
        this.decode = decode;
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
