//package bank.transaction.service.domainjpa;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import java.math.BigDecimal;
//import java.util.Date;
//
//
//@Entity
//@Table(name="account_statement")
//public class AccountStatement {
//    public AccountStatement(){ }
//
//    public AccountStatement(@NotNull Date startDate, @NotNull Date endDate, @NotNull String currency, @NotNull BigDecimal startBalance){
//        this.startDate = startDate;
//        this.endDate = endDate;
//        this.currency = currency;
//        this.startBalance = startBalance;
//    }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    @Version
//    private Long version;
//
//    @JsonProperty("start_date")
//    @NotNull
//    @Column(name = "start_date")
//    private Date startDate;
//
//    @JsonProperty("end_date")
//    @NotNull
//    @Column(name = "end_date")
//    private Date endDate;
//
//    @JsonProperty("currency")
//    @NotNull
//    @Column(name = "currency")
//    private String currency;
//
//    @JsonProperty("start_balance")
//    @NotNull
//    @Column(name = "start_balance")
//    private BigDecimal startBalance;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) { this.id = id; } //set id gak boleh ada
//
//    public Long getVersion() {
//        return version;
//    }
//
//    public void setVersion(Long version) { this.version = version; }
//
//    public Date getStartDate() { return startDate; }
//
//    public void setStartDate(Date startDate) { this.startDate = startDate; }
//
//    public Date getEndDate() { return endDate; }
//
//    public void setEndDate(Date endDate) { this.endDate = endDate; }
//
//    public String getCurrency() { return currency; }
//
//    public void setCurrency(String currency) { this.currency = currency; }
//
//    public BigDecimal getStartBalance() { return startBalance; }
//
//    public void setStartBalance(BigDecimal startBalance) { this.startBalance = startBalance; }
//
//    @Override
//    public String toString() {
//        return "AccountStatement{" +
//                "startDate='" + startDate + '\'' +
//                ", endDate='" + endDate + '\'' +
//                ", currency=" + currency +
//                ", startBalance='" + startBalance + '\'' +
//                '}';
//    }
//}
