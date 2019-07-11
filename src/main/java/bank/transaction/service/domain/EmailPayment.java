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

public class EmailPayment {
    @JsonProperty("method")
    private String method;

    @JsonProperty("vendor")
    private String vendor;

    @JsonProperty("vendor_icon")
    private String vendorIcon;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("account_number")
    private String accountNo;

    public void setMethod(String method) { this.method = method; }
    public String getMethod() { return method; }

    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getVendor() { return vendor; }

    public void setVendorIcon(String vendorIcon) { this.vendorIcon = vendorIcon; }
    public String getVendorIcon() { return vendorIcon; }

    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountName() { return accountName; }

    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public String getAccountNo() { return accountNo; }


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
