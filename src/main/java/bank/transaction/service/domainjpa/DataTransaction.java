//package bank.transaction.service.domainjpa;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import java.math.BigDecimal;
//
//@Entity
//@Table(name="data_transaction")
//public class DataTransaction {
//
//    public DataTransaction(){}
//
//    public DataTransaction(@NotNull AccountStatement accountStatement, @NotNull String transactionDate, @NotNull String branchCode, @NotNull String transactionType, @NotNull BigDecimal transactionAmount, @NotNull String transactionName, String trailer){
//        this.accountStatement = accountStatement;
//        this.transactionDate = transactionDate;
//        this.branchCode = branchCode;
//        this.transactionType = transactionType;
//        this.transactionAmount = transactionAmount;
//        this.transactionName = transactionName;
//        this.trailer = trailer;
//    }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    @Version
//    private Long version;
//
//    @ManyToOne(fetch=FetchType.LAZY)
//    @JoinColumn(name = "account_statement_id")
//    private AccountStatement accountStatement;
//
//    @Column(name = "transaction_date")
//    private String transactionDate; //saya juga gak tau kenapa transactionDate isinya PEND / string doang
//
//    @NotNull
//    @Column(name = "branch_code")
//    private String branchCode;
//
//    @NotNull
//    @Column(name = "transaction_type")
//    private String transactionType;
//
//    @NotNull
//    @Column(name = "transaction_amount")
//    private BigDecimal transactionAmount;
//
//    @NotNull
//    @Column(name = "transactionName")
//    private String transactionName;
//
//    @Column(name = "trailer", nullable = true)
//    private String trailer;
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
//    public AccountStatement getAccountStatement() { return accountStatement; }
//
//    public void setAccountStatement(AccountStatement accountStatement) { this.accountStatement = accountStatement; }
//
//    public String getTransactionDate() { return transactionDate; }
//
//    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
//
//    public String getBranchCode() { return branchCode; }
//
//    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
//
//    public String getTransactionType() { return transactionType; }
//
//    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
//
//    public BigDecimal getTransactionAmount() { return transactionAmount; }
//
//    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }
//
//    public void setTransactionName(String transactionName) { this.transactionName = transactionName; }
//
//    public String getTransactionName() { return transactionName; }
//
//    public void setTrailer(String trailer) { this.trailer = trailer; }
//
//    public String getTrailer() { return trailer; }
//
//    @Override
//    public String toString() {
//        return super.toString();
//    }
//}
