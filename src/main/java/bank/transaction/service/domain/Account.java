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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class Account {
    @JsonProperty("AccountNumber")
    private String no;

    @JsonProperty("Currency")
    private String currencyCode;

    @JsonProperty("Balance")
    private BigDecimal balance;

    @JsonProperty("AvailableBalance")
    private BigDecimal availableBalance;

    @JsonProperty("FloatAmount")
    private BigDecimal floatAmount;

    @JsonProperty("HoldAmount")
    private BigDecimal holdAmount;

    @JsonProperty("Plafon")
    private BigDecimal plafond;

    public String getNo() {
        return no;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getFloatAmount() {
        return floatAmount;
    }

    public BigDecimal getHoldAmount() {
        return holdAmount;
    }

    public BigDecimal getPlafond() {
        return plafond;
    }

    @Override
    public String toString() {
        return "Account{" +
                "no='" + no + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", balance=" + balance +
                ", availableBalance=" + availableBalance +
                ", floatAmount=" + floatAmount +
                ", holdAmount=" + holdAmount +
                ", plafond=" + plafond +
                '}';
    }
}
