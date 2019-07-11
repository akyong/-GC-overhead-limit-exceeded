/**
 * Copyright (c) 2019. PT. Distributor Indonesia Unggul. All rights reserverd.
 *
 * This source code is an unpublished work and the use of  a copyright  notice
 * does not imply otherwise. This source  code  contains  confidential,  trade
 * secret material of PT. Distributor Indonesia Unggul.
 * Any attempt or participation in deciphering, decoding, reverse  engineering
 * or in any way altering the source code is strictly  prohibited, unless  the
 * prior  written consent of Distributor Indonesia Unggul. is obtained.
 *
 * Unless  required  by  applicable  law  or  agreed  to  in writing, software
 * distributed under the License is distributed on an "AS IS"  BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or  implied.  See  the
 * License for the specific  language  governing  permissions  and limitations
 * under the License.
 *
 * Author : Bobby
 */
package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "account_statement_detail")
public class AccountStatementDetail {

    public AccountStatementDetail(){

    }

    public AccountStatementDetail(AccountStatement accountStatement, String transactionDate, String branchCode, TransactionType transactionType, BigDecimal amount,String name, String remark){
        this.accountStatement = accountStatement;
        this.transactionDate = transactionDate;
        this.branchCode = branchCode;
        this.transactionType = transactionType;
        this.amount = amount;
        this.name = name;
        this.remark = remark;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Column(name = "transaction_date")
    @JsonProperty("TransactionDate")
    private String transactionDate;

    @Column(name = "branch_code")
    @JsonProperty("BranchCode")
    private String branchCode;

    @Column(name = "transaction_type")
    @JsonProperty("TransactionType")
    private TransactionType transactionType;

    @Column(name = "transaction_amount")
    @JsonProperty("TransactionAmount")
    private BigDecimal amount;

    @Column(name = "transaction_name")
    @JsonProperty("TransactionName")
    private String name;

    @Column(name = "trailer")
    @JsonProperty("Trailer")
    private String remark;

    @JoinColumn(name = "account_statement_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    private AccountStatement accountStatement;

    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }

    public void setVersion(Long version) { this.version = version; }

    public Long getVersion() { return version; }

    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getBranchCode() {
        return branchCode;
    }

    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setName(String name) { this.name = name; }

    public String getName() {
        return name;
    }

    public void setRemark(String remark) { this.remark = remark; }

    public String getRemark() {
        return remark;
    }

    public void setAccountStatement(AccountStatement accountStatement) { this.accountStatement = accountStatement; }

    public AccountStatement getAccountStatement() { return accountStatement; }

    @Override
    public String toString() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    public String toString() {
//        return "'AccountStatementDetail':{" +
//                "'branchCode':'" + branchCode + '\'' +
//                ", 'transactionType':" + transactionType +
//                ", 'amount':" + amount +
//                ", 'name':" + name  +
//                ", 'remark':" + remark +
//                ", 'transactionDate':" + transactionDate +
//                '}';
//    }
}
