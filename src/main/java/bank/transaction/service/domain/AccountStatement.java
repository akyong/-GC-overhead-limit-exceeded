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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="account_statement")
public class AccountStatement {

    public AccountStatement(){ }

    public AccountStatement(@NotNull Date startDate, @NotNull Date endDate, @NotNull String currencyCode, @NotNull BigDecimal startBalance, List<AccountStatementDetail> accountStatementDetailList){
        this.startDate = startDate;
        this.endDate = endDate;
        this.currencyCode = currencyCode;
        this.startBalance = startBalance;
        this.accountStatementDetailList = accountStatementDetailList;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Column(name = "start_date")
    @JsonProperty("StartDate")
    @NotNull
    private Date startDate;

    @Column(name = "end_date")
    @JsonProperty("EndDate")
    @NotNull
    private Date endDate;

    @NotNull
    @Column(name = "currency")
    @JsonProperty("Currency")
    private String currencyCode;

    @NotNull
    @Column(name = "start_balance")
    @JsonProperty("StartBalance")
    private BigDecimal startBalance;

    @JsonIgnore
    @Column(name="bank")
    private String bank;

    @Nullable
    @OneToMany(mappedBy = "accountStatement")
    @JsonProperty("Data")
    private List<AccountStatementDetail> accountStatementDetailList;


    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }

    public void setVersion(Long version) { this.version = version; }

    public Long getVersion() { return version; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getStartDate() { return startDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getEndDate() { return endDate;}

    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCurrencyCode() { return currencyCode; }

    public void setStartBalance(BigDecimal startBalance) { this.startBalance = startBalance; }

    public BigDecimal getStartBalance() { return startBalance; }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBank() {
        return bank;
    }

    public List<AccountStatementDetail> getAccountStatementDetailList() {
        return accountStatementDetailList;
    }

    public void setAccountStatementDetailList(@Nullable List<AccountStatementDetail> accountStatementDetailList) {
        this.accountStatementDetailList = accountStatementDetailList;
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
//    @Override
//    public String toString() {
//        return "'AccountStatement{" +
//                "currencyCode'" + currencyCode + '\'' +
//                ", startBalance=" + startBalance +
//                ", accountStatementDetailList=" + accountStatementDetailList +
//                '}';
//    }

}
