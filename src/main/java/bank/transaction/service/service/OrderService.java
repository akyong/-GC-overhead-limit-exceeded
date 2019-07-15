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

package bank.transaction.service.service;

import bank.transaction.service.Common;
import bank.transaction.service.domain.*;
import bank.transaction.service.expedition.Order;
import bank.transaction.service.expedition.OrderDetail;
import bank.transaction.service.repository.OrderServiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.MediaType;
import io.micronaut.spring.tx.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.IntStream;

@Singleton
public class OrderService implements OrderServiceRepository {
    @Inject
    @Named("tokdis")
    DataSource dataSource;

    @Inject
    @Named("maintokdis")
    DataSource dataSourceTokdisdev;

    private final NotificationService notificationService;
    private final Common common;

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    public OrderService(NotificationService notificationService, Common common){
        this.common = common;
        this.notificationService = notificationService;
    }

    /**
     * Step #1
     * @param listamount from account statement detail.amount
     * */
    @Override
    @Transactional
    public void CheckToTokdis(List<BigDecimal> listamount){
        String amountin = getListAmount(listamount);
        ResultSet resultSet = null;
        List<Integer> orderIdLIst = new ArrayList<>();

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select a.*, b.supplier_data->'$.id' as supplier_id , a.reseller_data->'$.id' as reseller_id, date_format(b.awb_expired_at,'%d-%m %Y %h:%m') as awb_expired_at, date_format(b.supplier_feedback_expired_at,'%d-%m %Y %h:%m') as supplier_feedback_expired_at from order_summaries a join order_suppliers b on a.id = b.summaries_id where a.total_amount in "+amountin+" AND a.is_paid = 0 AND a.is_cancelled = 0 AND a.payment_expired_at >= ? AND (a.payment_status = 0 OR (a.payment_status =3 AND is_payment_uploaded = 1))")
            )
        {
            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                orderIdLIst.add(resultSet.getInt("id"));
            }

            if(orderIdLIst.size() > 0){
                updateToTokdis(orderIdLIst);
                updateOrderSuppliers(orderIdLIst);
            }


            try{
                HIT_API_TO_SERVER_AFTER_GET_STATEMENT(orderIdLIst);
            }
            catch (Exception e){

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getListAmount(List<BigDecimal> listamount){
        String id = "(";
        for(int a = 0 ; a< listamount.size(); a++){
            if(a == listamount.size()-1){
                id = id+listamount.get(a);
                id = id+")";
            }
            else{
                id = id+listamount.get(a);
                id = id+",";
            }
        }
        return id;
    }

    /**
     * TODO update order_summaries and order_supplier
     * Step #2
     * @param listId is field id from table order_summaries
     *
     * */

    /*
     * TODO update order_summaries
     * parameter payment_status -> PAID
     * parameter payment_verified_by -> system
     * parameter payment_verified_at ->current date time
     * is_paid -> 1 -> true/paid
     * */
    @Transactional
    public void updateToTokdis(List<Integer> listId){

        String idin = getInId(listId);
        LOG.info("idn ==> {}",idin);

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("update order_summaries set payment_status = ? ,payment_verified_by = ? ,payment_verified_at = ? , is_paid = ? where id in "+idin)
            )
        {
            preparedStatement.setInt(1,1);
            preparedStatement.setInt(2,0); //0 => system;
//            preparedStatement.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
            preparedStatement.setObject(3, NewDate());
            preparedStatement.setInt(4, 1);
//            preparedStatement.setInt(5, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set order_status = ? where summaries_id in "+idin)
            )
        {
            preparedStatement.setInt(1,1);//waiting confirm <DO NOT FORGET TO CHANGE THIS>
//            preparedStatement.setInt(2, id); // id summaries_id;
            preparedStatement.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * auto check expired time every 10s
     * */
    @Override
    @Transactional
    public void autoUpdatePaymentStatusIfExpired(){
        ResultSet resultSet = null;
        Order order = new Order();
        List<Integer> listId = new ArrayList<>();
        List<Integer> listResellerCode = new ArrayList<>();
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select * from order_summaries where payment_status in(0,2) AND payment_expired_at <= ? AND is_paid = 0 AND is_cancelled = 0");
            )
        {
            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                listId.add(resultSet.getInt("id"));//id order supplier
                listResellerCode.add(resultSet.getInt("reseller_code"));
//                HashMap<String,List> result = getOrderProductDetail(resultSet.getInt("id"));S
            }

            HashMap<String,List> result = getOrderProductDetail(listId);
            List<HashMap<String,Integer>> itemQtyList = result.get("itemQtyList");

            if(itemQtyList != null){
                String qlstringCase = "case ";
                String qlstringWhere = " where id in (";

                int lastloop = itemQtyList.size()-1;
                int index = 0 ;
                for (HashMap<String,Integer> map: itemQtyList) {
                    qlstringCase = qlstringCase +
                            "when id = "+map.get("id_item")+" then (quantity +"+map.get("qty")+") ";
                    if(lastloop == index){
                        qlstringWhere = qlstringWhere+map.get("id_item");
                    }
                    else{
                        qlstringWhere = qlstringWhere+map.get("id_item")+",";
                    }
                    index++;
                }
                qlstringCase = qlstringCase+"end";
                qlstringWhere = qlstringWhere+")";

                UpdateItemQuantity(qlstringCase,qlstringWhere);



//                List<OrderDetail> orderDetails =  result.get("orderDetailsList");
//                order =  getInformationToEmail(resultSet.getInt("reseller_code"),order);
//                order.setOrderNumber(resultSet.getString("order_number"));
//                order.setTotalPayment(resultSet.getInt("total_amount"));
//                order.setTotalPrice(resultSet.getInt("subtotal"));
//                order.setShippingAddress(getAddress(resultSet.getInt("id")));
//                order.setPpn(0);
//                order.setOrderDetailList(orderDetails);
//                SendMailNotification(
//                        order,"failed"
//                );
            }

            if(listId.size() > 0){
                updateOrderSummaries(listId);
                updateOrderStatusAtOrderSupplier(listId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @params id
     * update column
     * payment status => 2
     * is_cancelled = 1
     * */
    @Transactional
    public void updateOrderSummaries(List<Integer> listid){
        LOG.info("updateOrderSummaries({})",listid);

        String inId = getInId(listid);

        LOG.info("id to string ==>>> {}", inId);

        try
                (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("update order_summaries set payment_status = ? , " +
                            "is_cancelled = ? where id in "+inId)
                )
        {
            preparedStatement.setInt(1,2);
            preparedStatement.setInt(2,1);
//            preparedStatement.setString(3, inId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**1
     * @return string (1,2,3,4)
     * */
    public String getInId(List<Integer> listid){
        String id = "(";
        for(int a = 0 ; a< listid.size(); a++){
            if(a == listid.size()-1){
                id = id+listid.get(a);
                id = id+")";
            }
            else{
                id = id+listid.get(a);
                id = id+",";
            }
        }
        return id;
    }

    /**
     * update if ordrder expired
     * */
    @Transactional
    public void updateOrderStatusAtOrderSupplier(List<Integer> listid){
        LOG.info("updateOrderStatusAtOrderSupplier({})",listid);

        String inId = getInId(listid);

        try
                (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set order_status = 2 , reason_rejected = 'SYSTEM_REJECTED_REASON_0', rejected_at = ? , is_rejected_by_system = 1, is_rejected = 1 where summaries_id in "+inId)
                )
        {
            preparedStatement.setObject(1, NewDate());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO if payment is success, update supplier_feedback_expired_at and order_status
     * @params id -> summaries_id
     * */
    @Transactional
    public void updateOrderSuppliers(List<Integer> listid){
        String idin = getInId(listid);
        Statement statement = null;
        ResultSet resultSet = null;
        try
                (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set supplier_feedback_expired_at = DATE_ADD(SYSDATE(), INTERVAL 3 DAY)  ," +
                            "supplier_feedback_expired_time = UNIX_TIMESTAMP(DATE_ADD(SYSDATE(), INTERVAL 3 DAY)) ," +
                            "order_status = ? where summaries_id in "+idin);
                )
        {
            preparedStatement.setInt(1,1);
//            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update column order_status => Canceled
     * base on summeries_id frpm table order_supplier
     * */
    public void updateOrderSupplier(int id){
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        try {
            con = dataSource.getConnection();
            preparedStatement = con.prepareStatement("update order_suppliers set order_status = ? where summaries_id = ?");
            preparedStatement.setString(1,common.CANCELLED);//waiting confirm <DO NOT FORGET TO CHANGE THIS>
            preparedStatement.setInt(2, id); // id summaries_id;
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            try {
                if(resultSet != null) resultSet.close();
                if(statement != null) statement.close();
                if(con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void SendMailNotification(Order order, String status) throws Exception {
        String URL_TOKDIS ="http://13.250.223.74:3001/api/v1/email/transaction/"+status;
        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(order.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
//        LOG.info("\n\n\n Response => {}",response);
        in.close();
    }

    @Transactional
    public Order getInformationToEmail(int id,Order order){
        Statement statement = null;
        ResultSet resultSet = null;
        HashMap map = new HashMap();
//        String idin = getInId(listid);

        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select * from reseller where id = ? ");
            )
        {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                order.setEmailTo(resultSet.getString("email_address"));
                order.setCustomerName(resultSet.getString("fullname"));
                order.setPhoneNo(resultSet.getString("handphone"));

                map.put("emailTo",resultSet.getString("email_address"));
                map.put("customerName",resultSet.getString("fullname"));
                map.put("phoneNo",resultSet.getString("handphone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    @Transactional
    public String getAddress(int id){

        ResultSet resultSet = null;
        String alamat = "";
        HashMap map = new HashMap();

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select receiver->'$.address' as alamat from order_suppliers where summaries_id = ? LIMIT 1 ");

            )
        {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                alamat = resultSet.getString("alamat");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alamat;
    }

    @Transactional
    public  HashMap<String,List> getOrderProductDetail(List<Integer> listId){
        ResultSet resultSet = null;
        List<OrderDetail> tampung = new ArrayList<>();
        List<HashMap<String,Integer>> itemAndQty = new ArrayList<>();
        HashMap<String,List> result = new HashMap<>();

        if(listId.size() >= 1){
            String idIn = getInId(listId);
            try
                    (
                            Connection con = dataSource.getConnection();
                            PreparedStatement preparedStatement = con.prepareStatement("select a.product_data, a.product_variation->'$[*].id' as id_item, a.product_variation->'$[*].qty' as qty from order_products a join order_suppliers b on a.order_supplier_id = b.id join order_summaries c on b.summaries_id = c.id where c.id in "+idIn);
                    )
            {
                resultSet = preparedStatement.executeQuery();

                while(resultSet.next()){
                    String jsonString = resultSet.getString("product_data");
                    String getItemList = resultSet.getString("id_item");
                    String getQtyList = resultSet.getString("qty");
                    getItemList = getItemList.replace("[","").replace("]","").replace(" ","");
                    getQtyList = getQtyList.replace("[","").replace("]","").replace(" ","");
                    List<String> idItemList = new ArrayList<>(Arrays.asList(getItemList.split(",")));
                    List<String> qtyItemList = new ArrayList<>(Arrays.asList(getQtyList.split(",")));

                    IntStream.range(0, idItemList.size())
                            .forEach(idx ->
                                    {
                                        HashMap<String,Integer> map = new HashMap();
                                        map.put("id_item",Integer.parseInt(idItemList.get(idx)));
                                        map.put("qty",Integer.parseInt(qtyItemList.get(idx)));
                                        itemAndQty.add(map);
                                    }
                            );

                    ObjectMapper mapper = new ObjectMapper();
                    OrderDetail orderDetail = mapper.readValue(jsonString, OrderDetail.class);
                    tampung.add(orderDetail);

                }
                result.put("orderDetailsList",tampung);
                result.put("itemQtyList",itemAndQty);

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return result;
    }

    @Override
    @Transactional
    public void updateOrderStatusToDone(){
        List<InvoiceIdAndResellerCode> idOrderSup = getListOrderSupplierWantToBeDone();

//        idOrderSup.forEach(itx->{UpdateORderStatusOrderSupplier(itx);});
        try {
            HIT_API_TO_SERVER_AFTER_ORDER_IS_DONE(idOrderSup);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void UpdateORderStatusOrderSupplier(int id){

        Statement statement = null;
        ResultSet resultSet = null;


        try
                (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set order_status = ? , confirmed_at = ? where id = ? ");
                )
        {

            preparedStatement.setInt(1,6);//Order status -> DONE
            preparedStatement.setObject(2, NewDate());
            preparedStatement.setInt(3, id);//id -> itx
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Transactional
    public List<InvoiceIdAndResellerCode> getListOrderSupplierWantToBeDone(){
        ResultSet resultSet = null;

        List<InvoiceIdAndResellerCode> invoiceIdAndResellerCodeList = new ArrayList<>();

        try
                (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("select a.id, b.reseller_code from order_suppliers a join order_summaries b on a.summaries_id = b.id  where (a.confirmed_expired_at <= ? AND a.confirmed_at is null AND a.order_status = 5)");
                )
        {

            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                InvoiceIdAndResellerCode InvoiceIdAndResellerCode = new InvoiceIdAndResellerCode(resultSet.getInt("id"),resultSet.getInt("reseller_code"));
                invoiceIdAndResellerCodeList.add(InvoiceIdAndResellerCode);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return invoiceIdAndResellerCodeList;

    }
    @Transactional
    public void sendNotifWhereStatusIsDone(){
        Statement statement = null;
        ResultSet resultSet = null;

        try
                (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("select a.*, b.supplier_data->'$.id' as supplier_id from order_suppliers a join order_summaries b on a.summaries_id = b.id where a.confirmed_expired_at <= ? AND a.confirmed_at is null AND a.order_status = 5");
                )
        {
            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String messageSupplier = "Pesanan "+resultSet.getString("invoice_number")+" telah selesai. Dana akan terus diteruskan ke Saldo kamu.";
                notificationService.supplierNotification(common.LOG_NOTIF, "Pesanan selesai", messageSupplier, resultSet.getInt("supplier_id"),1 ,"ADMIN_TO_SUPPLIER" );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void updateOrderStatusRejected(){
        UpdateQuantityOfItemWhenRejectOrder();
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set is_rejected = 1 , order_status = 2, supplier_feedback_at = ? , is_rejected_by_system = 1, rejected_at = ? , reason_rejected = 'SYSTEM_REJECTED_REASON_1' where supplier_feedback_expired_at <= ? AND supplier_feedback_at is null AND order_status = 1 ");
            )
        {
            preparedStatement.setObject(1, NewDate());
            preparedStatement.setObject(2, NewDate());
            preparedStatement.setObject(3, NewDate());
            preparedStatement.executeUpdate();

            ListIdInvoiceId listIdInvoiceId = new ListIdInvoiceId();
            listIdInvoiceId.setInvoiceId(GET_LIST_INVOICE_ID_FOR_STATUS_REJECTED());
            notificationService.NOTIFICATION_TRX_FAILED(common.LOG_NOTIF,listIdInvoiceId);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public List<Integer> GET_LIST_INVOICE_ID_FOR_STATUS_REJECTED(){
        ResultSet resultSet = null;
        List<Integer> listId = new ArrayList<>();
        try
                (
                        Connection con = dataSource.getConnection();
                        PreparedStatement preparedStatement = con.prepareStatement("select * from order_suppliers where supplier_feedback_expired_at <= ? AND supplier_feedback_at is null AND order_status = 1 ");
                )
        {
            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                listId.add(resultSet.getInt("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return listId;
    }

    @Transactional
    public List<HashMap<String,Integer>> getAllItemWhereWantToUpdateIt(){
        ResultSet resultSet = null;
        List<HashMap<String,Integer>> itemAndQty = new ArrayList<>();
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select b.product_variation->'$[*].id' as id_item, b.product_variation->'$[*].qty' as qty from order_suppliers a join order_products b on b.order_supplier_id = a.id where a.supplier_feedback_expired_at < ? AND a.supplier_feedback_at is null AND a.order_status = 1");
            )
        {
            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                HashMap<String,Integer> map = new HashMap();

                Array item = resultSet.getArray("id_item");
                Array qty = resultSet.getArray("qty");
                int[] listIdItem = (int[])item.getArray();
                int[] listQty = (int[])qty.getArray();

                for(int a=0;a<listIdItem.length;a++)
                {
                    map.put("id_item",listIdItem[a]);
                    map.put("qty",listQty[a]);
                    itemAndQty.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return itemAndQty;
    }

    @Transactional
    public void UpdateQuantityOfItemWhenRejectOrder(){
        List<HashMap<String,Integer>> itemQtyList = getAllItemWhereWantToUpdateIt(); //METHOD
        String qlstringCase = "case ";
        String qlstringWhere = " where id in (";

        if(!itemQtyList.isEmpty()){
            int lastloop = itemQtyList.size()-1;
            int index = 0 ;
            for (HashMap<String,Integer> map: itemQtyList) {
                qlstringCase = qlstringCase +
                        "when id = "+map.get("id_item")+" then (quantity -"+map.get("qty")+") ";
                if(lastloop == index){
                    qlstringWhere = qlstringWhere+map.get("id_item");
                }
                else{
                    qlstringWhere = qlstringWhere+map.get("id_item")+",";
                }

                index++;
            }
            qlstringCase = qlstringCase+"end";
            qlstringWhere = qlstringWhere+")";
            UpdateItemQuantity(qlstringCase,qlstringWhere);
        }
    }

    @Override
    @Transactional
    public void UpdateIsRejectedIfSupplierNotSentTheOrder(){
        /**
         * @NOTED + brankas
         * */
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set is_shipped = 1, order_status = 2, is_rejected_by_system = 1, is_rejected = 1 ,rejected_at = ? , reason_rejected = 'SYSTEM_REJECTED_REASON_2' where awb_expired_at <= ? AND supplier_feedback_at is not null AND awb_number = '' AND order_status = 3  ");
            )
        {
            preparedStatement.setObject(1, NewDate());
            preparedStatement.setObject(2, NewDate());
            preparedStatement.executeUpdate();

            ListIdInvoiceId listIdInvoiceId = new ListIdInvoiceId();
            listIdInvoiceId.setInvoiceId(GET_LIST_INVOICE_ID());
            notificationService.NOTIFICATION_TRX_FAILED(common.LOG_NOTIF,listIdInvoiceId);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public List<Integer> GET_LIST_INVOICE_ID(){
        ResultSet resultSet = null;
        List<Integer> listId = new ArrayList<>();

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select * from order_suppliers where awb_expired_at <= ? AND supplier_feedback_at is not null AND awb_number = '' AND order_status = 3  ");
            )
        {
            preparedStatement.setObject(1, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next())
            {
                listId.add(resultSet.getInt("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return listId;
    }

    @Transactional
    public List<Integer> CheckOrderSummariesGetAllCompletedIsNull(){

        Statement statement = null;
        ResultSet resultSet = null;

        List<Integer> listOrderSummaries = new ArrayList<>();

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select * from order_summaries where completed_at is null ")
            )
        {
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                listOrderSummaries.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOrderSummaries;
    }

    @Transactional
    public void UpdateItemQuantity(String qlStringCase, String qlStringWhere){
        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("update product_variation_details SET quantity = "+ qlStringCase + qlStringWhere);
            )
        {
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void checkForReminder(){
        Email email = new Email();
        EmailPayment emailPayment = new EmailPayment();
        List<EmailPayment> emailPaymentList = new ArrayList<>();

        try
            (
                    Connection con = dataSource.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("select a.id, a.payment_expired_at, a.order_number, a.total_amount, a.subtotal, a.transfer_unique_number, a.payment_data->'$.method' as method, a.payment_data->'$.via.BANK[0].code' as vendor, a.payment_data->'$.via.BANK[0].icon' as vendor_icon, a.payment_data->'$.via.BANK[0].name' as account_name, a.payment_data->'$.via.BANK[0].number' as account_number, b.shipping_data->'$.price' as shipping_fee, b.supplier_data->'$.id' as supplier_id, a.reseller_data->'$.id' as reseller_id from order_summaries a join order_suppliers b on a.id = b.summaries_id  LEFT join notifications d  on (a.id <> d.cols_id and codes = 'PAYMENT_REMINDER' and types = 'ONE_SIGNAL' ) where a.payment_status = 0 AND a.is_paid = 0 AND a.is_cancelled = 0 AND a.payment_expired_at <= DATE_SUB(? , interval -1 hour)");
            )
        {
            preparedStatement.setObject(1, NewDate());
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Timestamp timestamp = resultSet.getTimestamp("payment_expired_at");
                Date paymentExpiredAt = new Date(timestamp.getTime());
                String paymentExpired = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(paymentExpiredAt);
                int totalAmount = resultSet.getInt("total_amount");
                String orderNo = resultSet.getString("order_number");

                String tanggal = new SimpleDateFormat("EEEE, dd MMMM yyyy").format(paymentExpiredAt);
                String jam = new SimpleDateFormat("hh.mm").format(paymentExpiredAt);
                //TODO set values to EmailPayment
                emailPayment.setMethod(resultSet.getString("method").replace("\"","").replace("\"",""));
                emailPayment.setVendor(resultSet.getString("vendor").replace("\"","").replace("\"",""));
                emailPayment.setVendorIcon(resultSet.getString("vendor_icon").replace("\"","").replace("\"",""));
                emailPayment.setAccountName(resultSet.getString("account_name").replace("\"","").replace("\"",""));
                emailPayment.setAccountNo(resultSet.getString("account_number").replace("\"","").replace("\"",""));
                emailPaymentList.add(emailPayment);

                //TODO set values to Object
                email.setEmailPaymentList(emailPaymentList);
                email.setLastPaymentDate(tanggal);
                email.setLastPaymentHour(jam);
                email.setShippingFee(resultSet.getInt("shipping_fee"));
                email.setOrderNumber(resultSet.getString("order_number"));
                email.setTotalPayment(resultSet.getInt("total_amount"));
                email.setSubtotal(resultSet.getInt("subtotal"));
                email.setUniqueCode(resultSet.getString("transfer_unique_number"));

//                String supplierId = resultSet.getString("supplier_id").replace("\"","").replace("\"","").replace(" ","");
                String resellerId = resultSet.getString("reseller_id").replace("\"","").replace("\"","").replace(" ","");


                Email updateValues = GetEmailOrderDetailForReminder(resultSet.getInt("id"), email);

                updateValues = getEmailInformation(resultSet.getInt("reseller_id"), updateValues);
//                LOG.info("\nemail -> {}",updateValues.toString());
//                SendMailConfirm(updateValues, "reminder");
                String messageText ="Segera lakukan pembayaran sebesar Rp. "+totalAmount+" untuk pesananmu "+orderNo+" sebelum "+paymentExpired+" untuk menghindari pembatalan.";

//                sendNotifOneSignalSupplierForReminder(Integer.parseInt(supplierId), messageText);
//                sendNotifOneSignalResellerForReminder(Integer.parseInt(resellerId),messageText);//ini yang diuncoment buat cek sudah keinsert aatau belum tanggal 19 juli 2019

//                CreateLogNotificationForReminder(resultSet.getInt("id"));


            }

            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public Email GetEmailOrderDetailForReminder(int id,Email email){
        ResultSet resultSet = null;
        List<EmailOrderDetail> emailOrderDetailList = new ArrayList<>();
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select a.product_variation->'$[*].name' as name, a.product_data->'$.price' as price, a.product_variation->'$[*].qty' as qty from order_products a join order_suppliers b on a.order_supplier_id = b.id join order_summaries c on b.summaries_id = c.id where c.id = ?");
            )
        {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
               String getListName = resultSet.getString("name");
               String getListQty = resultSet.getString("qty");
               int price = resultSet.getInt("price");

               getListName = getListName.replace("[","").replace("]","").replace(" ","").replace("\"","").replace("\"","");
               getListQty = getListQty.replace("[","").replace("]","").replace(" ","").replace("\"","").replace("\"","");

               List<String> itemNameList = new ArrayList<>(Arrays.asList(getListName.split(",")));
               List<String> qtyItemList = new ArrayList<>(Arrays.asList(getListQty.split(",")));

                IntStream.range(0, itemNameList.size())
                        .forEach(idx ->
                                {
                                    EmailOrderDetail emailOrderDetail = new EmailOrderDetail();
                                    emailOrderDetail.setName(itemNameList.get(idx));
                                    emailOrderDetail.setPrice(price);
                                    emailOrderDetail.setQty(Integer.parseInt(qtyItemList.get(idx)));

                                    emailOrderDetail.setTotal(price * Integer.parseInt(qtyItemList.get(idx)));
                                    emailOrderDetailList.add(emailOrderDetail);
                                }
                        );
                email.setEmailOrderDetailList(emailOrderDetailList);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return email;
    }


    @Transactional
    public Email getEmailInformation(int id, Email email) throws IOException {
        ResultSet resultSet = null;

        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select * from reseller where id = ? ");
            )
        {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                email.setEmailTo(resultSet.getString("email_address"));
                email.setCustomerName(resultSet.getString("fullname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return email;
    }

    @Transactional
    public void SendMailConfirm(Email email, String status) throws Exception {
        String URL_TOKDIS ="http://13.250.223.74:3001/api/v1/email/payment/"+status;
        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(email.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
//        LOG.info("\n\n\n Response => {}",response);
        in.close();
    }

    public void sendNotifOneSignalSupplierForReminder(int supplierId, String message) throws Exception {
        String URL_TOKDIS ="http://13.250.223.74:3001/api/v1/notification/supplier-onesignal";
        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        OneSignal oneSignal = new OneSignal();
        oneSignal = getSupplierIDForSendOneSignal(oneSignal,supplierId);
        oneSignal.setMessage(message);
        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
//        LOG.info("\n asdf {}",oneSignal.toString());
        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(oneSignal.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
//        LOG.info("\n\n\n Response => {}",response);
        in.close();
    }

    public void sendNotifOneSignalSupplierForReminderUSINGSUPPLIERCODE(OneSignal oneSignal) throws Exception {
        String URL_TOKDIS ="http://13.250.223.74:3001/api/v1/notification/onesignal";
        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(oneSignal.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
//        LOG.info("\n\n\n Response => {}",response);
        in.close();
    }

    public void sendNotifOneSignalResellerForReminder(int resellerId, String message) throws Exception {
        String URL_TOKDIS ="http://13.250.223.74:3001/api/v1/notification/onesignal";
        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        OneSignal oneSignal = new OneSignal();
        oneSignal = getResellerIDForSendOneSignal(oneSignal,resellerId);
        oneSignal.setMessage(message);
        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(oneSignal.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
//        LOG.info("\n\n\n Response => {}",response);
        in.close();
    }


    @Transactional
    public OneSignal getSupplierIDForSendOneSignal(OneSignal oneSignal, int id){//supplier id
        ResultSet resultSet = null;

        HashMap map = new HashMap();

        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select player_id from supplier where id = ? ");
            )
        {

            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                oneSignal.setPlayerId(resultSet.getString("player_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return oneSignal;

    }

    //TODO get Supplier -> player_id
    @Transactional
    public OneSignal getSupplierCodeForSendOneSignal(OneSignal oneSignal, String code){//supplier code
        ResultSet resultSet = null;


        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select player_id from supplier where member_code = ? ");
            )
        {
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                oneSignal.setPlayerId(resultSet.getString("player_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oneSignal;

    }

    @Transactional
    public OneSignal getResellerIDForSendOneSignal(OneSignal oneSignal, int id){//supplier id
        ResultSet resultSet = null;
        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select player_id from onesignal_player where reseller_id = ? ");
            )
        {

            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                oneSignal.setPlayerId(resultSet.getString("player_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return oneSignal;

    }

    @Transactional
    public void sendSMSNotification(String phoneNo, String message) throws Exception {
        String URL_TOKDIS ="http://13.250.223.74:3001/api/v1/notification/sms";
        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        Sms sms = new Sms();
        sms.setPhoneNo(phoneNo);
        sms.setMessage(message);

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(sms.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    public String getPhoneNoSupplierById(int id){//supplier id
        ResultSet resultSet = null;

        String phoneNo = "";
        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select handphone_no from supplier where id = ? ");
            )
        {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                phoneNo = resultSet.getString("handphone_no");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return phoneNo;

    }

    @Transactional
    public void sentNotifMustSentItem(){
        ResultSet resultSet = null;

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM ( SELECT o.*,DATE_FORMAT(o.supplier_feedback_expired_at, '%d-%m-%Y %H.%i') as send_before, (select count(1) FROM notifications n where n.cols_id = o.id AND n.codes = 'SEND_ORDER' AND types = 'ONE_SIGNAL' ) AS is_sent FROM order_suppliers o where order_status = 3 and supplier_feedback_expired_at > ? AND  date_add(supplier_feedback_expired_at, interval -1 day) < ? ) A where is_sent =0");
            )
        {
            preparedStatement.setObject(1, NewDate());
            preparedStatement.setObject(2, NewDate());
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String supplierCode = resultSet.getString("supplier_code");
                String message = "Pesan "+resultSet.getString("invoice_number")+" menunggu dikirim. Segera kirim dan input resi sebelum "+resultSet.getString("send_before");
                OneSignal oneSignal = new OneSignal();
                oneSignal = getSupplierCodeForSendOneSignal(oneSignal,supplierCode);
                oneSignal.setMessage(message);
                sendNotifOneSignalSupplierForReminderUSINGSUPPLIERCODE(oneSignal);

                int supplierId = getSupplierIDByCodeForSendOneSignal(supplierCode);

                notificationService.supplierNotification(common.LOG_NOTIF, "Notifikasi Segera Proses", message,supplierId,1 ,"ADMIN_TO_SUPPLIER" );
                CreateLogNotification(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void CreateLogNotification(int orderSupplierId){
        Statement statement = null;
        ResultSet resultSet = null;

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("insert into notifications(cols_id,codes,types,created_by,created_at) values(?, 'SEND_ORDER','ONE_SIGNAL',0,? )");
            )
        {
            preparedStatement.setInt(1,orderSupplierId);
            preparedStatement.setObject(2, NewDate());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void CreateLogNotificationForReminder(int orderSummariesId){
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("insert into notifications(cols_id,codes,types,created_by,created_at) values(?, 'PAYMENT_REMINDER','ONE_SIGNAL',0,? )");
            )
        {

            preparedStatement.setInt(1,orderSummariesId);
            preparedStatement.setObject(2, NewDate());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //TODO get Supplier -> player_id
    @Transactional
    public Integer getSupplierIDByCodeForSendOneSignal(String code){//supplier code
        Statement statement = null;
        ResultSet resultSet = null;

        int supplierId = 0;

        try
            (
                Connection con = dataSourceTokdisdev.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select id from supplier where member_code = ? ");
            )
        {
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                supplierId = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return supplierId;
    }

    @Transactional
    public Email getSentEmail(int id, String status){

        Statement statement = null;
        ResultSet resultSet = null;

        Email email = new Email();
        EmailPayment emailPayment = new EmailPayment();
        List<EmailPayment> emailPaymentList = new ArrayList<>();

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("select a.id,a.total_delivery_fee, a.payment_expired_at, a.order_number, a.total_amount, a.subtotal, a.transfer_unique_number, a.payment_data->'$.method' as method, a.payment_data->'$.via.BANK[0].code' as vendor, a.payment_data->'$.via.BANK[0].icon' as vendor_icon, a.payment_data->'$.via.BANK[0].name' as account_name, a.payment_data->'$.via.BANK[0].number' as account_number,b.supplier_data->'$.id' as supplier_id, a.reseller_data->'$.id' as reseller_id from order_summaries a join order_suppliers b on a.id = b.summaries_id where a.payment_status = 1 AND a.is_paid = 1 AND a.is_cancelled = 0 and a.id = ?");
            )
        {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Timestamp timestamp = resultSet.getTimestamp("payment_expired_at");
                Date paymentExpiredAt = new Date(timestamp.getTime());

                String tanggal = new SimpleDateFormat("EEEE, dd MMMM yyyy").format(paymentExpiredAt);
                String jam = new SimpleDateFormat("hh.mm").format(paymentExpiredAt);
                //TODO set values to EmailPayment
                emailPayment.setMethod(resultSet.getString("method").replace("\"","").replace("\"",""));
                emailPayment.setVendor(resultSet.getString("vendor").replace("\"","").replace("\"",""));
                emailPayment.setVendorIcon(resultSet.getString("vendor_icon").replace("\"","").replace("\"",""));
                emailPayment.setAccountName(resultSet.getString("account_name").replace("\"","").replace("\"",""));
                emailPayment.setAccountNo(resultSet.getString("account_number").replace("\"","").replace("\"",""));
                emailPaymentList.add(emailPayment);
                //TODO set values to Object
                email.setEmailPaymentList(emailPaymentList);
                email.setLastPaymentDate(tanggal);
                email.setLastPaymentHour(jam);
                email.setOrderNumber(resultSet.getString("order_number"));
                email.setTotalPayment(resultSet.getInt("total_amount"));
                email.setSubtotal(resultSet.getInt("subtotal"));
                email.setUniqueCode(resultSet.getString("transfer_unique_number"));
                email.setShippingFee(resultSet.getInt("total_delivery_fee"));

                Email updateValues = GetEmailOrderDetailForReminder(resultSet.getInt("id"), email);
                updateValues = getEmailInformation(resultSet.getInt("reseller_id"), updateValues);

                SendMailConfirm(updateValues, status);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return email;

    }

    public void HIT_API_TO_SERVER_AFTER_GET_STATEMENT(List<Integer> listOrderId) throws Exception {
        String URL_TOKDIS = "";
        if (common.LOG_NOTIF=="dev")
        {
            URL_TOKDIS = "https://transaction.tokodistributor.net/notification/order_paid";
        }
        else if (common.LOG_NOTIF=="prod")
        {
            URL_TOKDIS = "https://transaction.tokodistributor.com/notification/order_paid";
        }

        URL obj = new URL(URL_TOKDIS);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
        ListIdOrderId listIdOrderId = new ListIdOrderId();
        listIdOrderId.setOrderId(listOrderId);

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(listIdOrderId.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    public void HIT_API_TO_SERVER_AFTER_ORDER_IS_DONE(List<InvoiceIdAndResellerCode> invoiceIdAndResellerCodeList) throws Exception {
        String URL_TOKDIS = "";
        if (common.LOG_NOTIF=="dev")
        {
            URL_TOKDIS = "https://transaction.tokodistributor.net/confirm/accept_automatic";
        }
        else if (common.LOG_NOTIF=="prod")
        {
            URL_TOKDIS = "https://transaction.tokodistributor.com/confirm/accept_automatic";
        }

        URL obj = new URL(URL_TOKDIS);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

        ObjectMapper mapper = new ObjectMapper();

        String jsonformat = mapper.writeValueAsString(invoiceIdAndResellerCodeList);

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(jsonformat);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    @Override
    @Transactional
    public String COMPLETE_TRX(@NotNull List<Integer> idList){
        ResponseModel response = new ResponseModel();
        try {

            updateToTokdis(idList);
            updateOrderSuppliers(idList);

            HIT_API_TO_SERVER_AFTER_GET_STATEMENT(idList);

            response.setCode(200);
            response.setCodeMessage("Success");
            response.setCodeType("success");
            response.setData("{}");
        }
        catch (Exception ex){
            response.setCode(400);
            response.setCodeMessage("Failed");
            response.setCodeType("failed");
            response.setData("{}");
            ex.printStackTrace();
        }
        return response.toString();
    }

//    @Override
    @Transactional
    public void updateAccountStatementDetail(int id){
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("UPDATE tokdis_statements.account_statement_detail SET created_at = ? WHERE id = ?");
            )
        {
            preparedStatement.setObject(1, NewDate());
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date NewDate() throws ParseException {

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
//        TimeZone timeZone = TimeZone.getTimeZone("Asia/Jakarta");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        //Local time zone
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

        //Time in GMT
//        LOG.info("Date => {}", dateFormatLocal.parse(dateFormatGmt.format(new Date())));
        return dateFormatLocal.parse(dateFormatGmt.format(new Date()));
    }

}
