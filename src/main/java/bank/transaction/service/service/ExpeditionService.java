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

import bank.transaction.service.domain.OneSignal;
import bank.transaction.service.expedition.Expedition;
import bank.transaction.service.repository.ExpeditionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.MediaType;
import io.micronaut.spring.tx.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Singleton
public class ExpeditionService implements ExpeditionRepository {
    @Inject
    @Named("tokdis")
    DataSource dataSource; // "warehouse" will be injected

    @Inject
    @Named("maintokdis")
    DataSource dataSourceTokdisdev;

    private static final Logger LOG = LoggerFactory.getLogger(AccountStatementService.class);
    private final String HOST_NAME= "http://13.250.223.74:3002";
    private final String PATH_TRACK= "/api/v1/tracking/";
    protected Expedition expedition;

    public ExpeditionService(Expedition expedition){this.expedition = expedition; }

    @Override
    @Transactional
    public void CheckTracking() throws Exception {
        List<HashMap<String,String>> hashMapList =  getListOfAwbNumber();
        String url = "";
        for (HashMap<String,String> list: hashMapList) {

            String trimResi = list.get("awbNumber").replace(" ","").replace(" ","");
            url = HOST_NAME+PATH_TRACK+list.get("kurir")+"/"+trimResi;
            LOG.info("URL => {}, END with comma", url);
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            connection.setRequestMethod("GET");

            //add request header
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            expedition = new ObjectMapper().readValue(response.toString(), Expedition.class);

            /*
             * DO
             * delivery_status = "DELIVERED"
             * order_status = "WAITING CONFIRMATION APALAH"
             * comfirmed_expired_at = delivered_at + 48 hours
             * is_delivered = 1
             * delivered_at = new date
             * */

            if(expedition.getDelivered()){
                try{
                    String str = list.get("kurir");
                    if(str.equals("jet"))str="jnt";
                    updateOrderSupplierIfTrackedNumber(list.get("awbNumber"), str);
                    String messageText ="Pesananmu "+list.get("orderNumber")+" telah sampai. Silahkan konfirmasi penerimaan pesananmu.";
                    sendNotifOneSignalResellerForReminder(Integer.parseInt(list.get("reseller_id")), messageText);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

//            try {
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }

        }
    }

    public List<HashMap<String,String>> getListOfAwbNumber(){
        List<HashMap<String,String>> listawbnumber = new ArrayList<>();
        Statement statement = null;
        ResultSet resultSet = null;
        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("SELECT a.shipping_data->'$.name' as kurir, a.awb_number, b.reseller_data->'$.id' as reseller_id, a.invoice_number FROM order_suppliers a "+
                        "JOIN order_summaries b on (a.summaries_id = b.id) WHERE b.payment_status = 1 AND b.is_paid = 1 AND a.supplier_feedback_at is not null "+
                        "AND a.is_delivered = 0 AND a.order_status in (4,5)");
            )
        {
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                HashMap<String, String> map = new HashMap<>();
                String str = resultSet.getString("kurir").replace("\"", "").toLowerCase();


                map.put("kurir",str);
                map.put("awbNumber",resultSet.getString("awb_number"));
                map.put("reseller_id",resultSet.getString("reseller_id"));
                map.put("orderNumber",resultSet.getString("invoice_number"));
                listawbnumber.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listawbnumber;
    }

    /*
     * DO
     * delivery_status = "DELIVERED"
     * order_status = "WAITING CONFIRMATION APALAH"
     * confirmed_expired_at = delivered_at + 48 hours
     * is_delivered = 1
     * delivered_at = new date
     * */
    public void updateOrderSupplierIfTrackedNumber(String awbnumber, String kurir){

        Statement statement = null;
        ResultSet resultSet = null;

        try
            (
                Connection con = dataSource.getConnection();
                PreparedStatement preparedStatement = con.prepareStatement("update order_suppliers set " +
                        "delivery_status = ? , " +
                        "order_status = ? , " +
                        "confirmed_expired_at = DATE_ADD(NOW(), INTERVAL 2 DAY) , " +
                        "is_delivered = ? , " +
                        "delivered_at =  NOW() " +
                        "where awb_number = ? AND shipping_data->'$.name' = ? ");
            )
        {




            preparedStatement.setInt(1,1);
            preparedStatement.setInt(2,5);//waiting confirmation
            preparedStatement.setInt(3, 1);
            preparedStatement.setString(4, awbnumber); // id awb_numer;
            preparedStatement.setString(5, kurir.toUpperCase()); // id awb_numer;
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO ONESIGNAL - Pesanan Sampai -
     * Message "Pesananmu INV/20190225/00000005 telah sampai. Silahkan konfirmasi penerimaan pesananmu."
     * */
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
        in.close();
    }

    public OneSignal getResellerIDForSendOneSignal(OneSignal oneSignal, int id){//resller id
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        HashMap map = new HashMap();

        String playerId = "";
        try {

            con = dataSourceTokdisdev.getConnection();
            preparedStatement = con.prepareStatement("select player_id from onesignal_player where reseller_id = ? ");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                oneSignal.setPlayerId(resultSet.getString("player_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return oneSignal;

    }

    public OneSignal getSupplierIDForSendOneSignal(OneSignal oneSignal, int id){//supplier id
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        try {
            con = dataSourceTokdisdev.getConnection();
            preparedStatement = con.prepareStatement("select player_id from supplier where id = ? ");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                oneSignal.setPlayerId(resultSet.getString("player_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return oneSignal;

    }

}
