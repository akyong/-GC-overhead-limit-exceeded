package bank.transaction.service.service;

import bank.transaction.service.domain.ListIdInvoiceId;
import bank.transaction.service.domain.NotificationReseller;
import bank.transaction.service.domain.NotificationSupplier;
import io.micronaut.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationService {
    @Inject
    @Named("tokdis")
    DataSource dataSource;

    @Inject
    @Named("maintokdis")
    DataSource dataSourceTokdisdev;
    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(){
    }

    public void supplierNotification(String option, String title, String content, int supplierId, int fromId, String remark) throws Exception {
        String URL_TOKDIS = "";
        if (option=="dev")
        {
            URL_TOKDIS = "http://13.250.223.74:3003/api/v1/supplier/notificationcenter/create";
        }
        else if (option=="prod")
        {
            URL_TOKDIS = "https://notification.tokodistributor.com/api/v1/supplier/notificationcenter/create";
        }

        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        NotificationSupplier notif = new NotificationSupplier();
        notif.setTitle(title);
        notif.setContent(content);
        notif.setSupplierId(supplierId);
        notif.setFromId(fromId);
        notif.setRemark(remark);

        //add request header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(notif.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        LOG.info("\n\n\n Response => {}", response);
        in.close();
    }

    public void resellerNotification(String option, String title, String content, int resellerId, int fromId, String remark) throws Exception{
        String URL_TOKDIS = "";
        if (option=="dev")
        {
            URL_TOKDIS = "http://13.250.223.74:3003/api/v1/supplier/notificationcenter/create";
        }
        else if (option=="prod")
        {
            URL_TOKDIS = "https://notification.tokodistributor.com/api/v1/supplier/notificationcenter/create";
        }

        URL obj = new URL(URL_TOKDIS);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        NotificationReseller notif = new NotificationReseller();
        notif.setTitle(title);
        notif.setContent(content);
        notif.setResellerId(resellerId);
        notif.setRemark(remark);
        notif.setFromId(fromId);

        //add reuqest header
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
        connection.setRequestMethod("POST");

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(notif.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        LOG.info("\n\n\n Response => {}", response);
        in.close();
    }

    public void NOTIFICATION_TRX_FAILED(String option, ListIdInvoiceId listIdInvoiceId) throws IOException {
        String URL_TOKDIS = "";
        if (option=="dev")
        {
            URL_TOKDIS = "https://transaction.tokodistributor.net/notification/order_rejected";
        }
        else if (option=="prod")
        {
            URL_TOKDIS = "https://transaction.tokodistributor.com/notification/order_rejected";
        }

        URL obj = new URL(URL_TOKDIS);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
        connection.setRequestMethod("POST");

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(listIdInvoiceId.toString());
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
}
