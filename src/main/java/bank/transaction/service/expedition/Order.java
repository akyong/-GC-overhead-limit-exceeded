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

package bank.transaction.service.expedition;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class Order {

    public Order(){}

    public Order(String orderNumber, String emailTo, String customerName, String shippingAddress, String phoneNo, int totalPrice, int totalPayment, int ppn, List<OrderDetail> orderDetailList){
        this.orderNumber = orderNumber;
        this.emailTo = emailTo;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.phoneNo = phoneNo;
        this.totalPrice = totalPrice;
        this.totalPayment = totalPayment;
        this.ppn = ppn;
        this.orderDetailList = orderDetailList;
    }

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("email_to")
    private String emailTo;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("phone_number")
    private String phoneNo;

    @JsonProperty("total_price")
    private int totalPrice;

    @JsonProperty("total_payment")
    private int totalPayment;

    @JsonProperty("ppn")
    private int ppn;

    @JsonProperty("order_detail")
    private List<OrderDetail> orderDetailList;

    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOrderNumber() { return orderNumber; }

    public void setEmailTo(String emailTo) { this.emailTo = emailTo; }
    public String getEmailTo() { return emailTo; }

    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerName() { return customerName; }

    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getShippingAddress() { return shippingAddress; }

    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
    public String getPhoneNo() { return phoneNo; }

    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
    public int getTotalPrice() { return totalPrice; }

    public void setTotalPayment(int totalPayment) { this.totalPayment = totalPayment; }
    public int getTotalPayment() { return totalPayment; }

    public void setPpn(int ppn) { this.ppn = ppn; }
    public int getPpn() { return ppn; }

    public void setOrderDetailList(List<OrderDetail> orderDetailList) { this.orderDetailList = orderDetailList; }
    public List<OrderDetail> getOrderDetailList() { return orderDetailList; }

//    @Override
//    public String toString() {
//        return "Order{" +
//                "orderNumber='" + orderNumber + '\'' +
//                ", emailTo='" + emailTo + '\'' +
//                ", customerName='" + customerName +'\'' +
//                ", shippingAddress='" + shippingAddress +'\'' +
//                ", phoneNo=" + phoneNo +
//                ", totalPrice=" + totalPrice +
//                ", totalPayment=" + totalPayment +
//                ", ppn=" + ppn +
//                '}';
//    }

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
