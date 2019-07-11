package bank.transaction.service.controller;


import bank.transaction.service.BniEncryptionClass;
import bank.transaction.service.domain.virtualaccount.VirtualAccount;
import bank.transaction.service.helper.Common;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

@Singleton
public class VirtualAccountPost {
    private Logger LOG = LoggerFactory.getLogger(VirtualAccountPost.class);
    protected String BNI_DEV(String trx_id, String trx_amount, String billing_type, String customer_name, String customer_email, String customer_phone, String virtual_account, String datetime_expired, String description ) throws Exception {

        BniEncryptionClass hash = new BniEncryptionClass();
        String URL_BNI_DEV = Common.URL_BNI_DEV;
        String converted = "";
        String type = "createbilling";
        String client_id = "00629";
        String data = "{\"type\":\""+type+"\",\"client_id\":\""+client_id+"\",\"trx_id\":\""+trx_id+"\",\"trx_amount\":\""+trx_amount+"\",\"billing_type\":\""+billing_type+"\",\"customer_name\":\""+customer_name+"\",\"customer_email\":\""+customer_email+"\",\"customer_phone\":\""+customer_phone+"\",\"virtual_account\":\""+virtual_account+"\",\"datetime_expired\":\""+datetime_expired+"\",\"description\":\""+description+"\"}";
        String cid = "00629";
        String key = "b04964206026f28683749f7d36afe871";
        String parsedData = hash.hashData(data,cid,key);
        LOG.info(parsedData);
        data = "{\"client_id\":"+" "+"\""+cid+"\",\"data\":"+" "+"\""+parsedData+"\"}";

        URL obj = new URL(URL_BNI_DEV);
        HttpsURLConnection postConnection = (HttpsURLConnection) obj.openConnection();

        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

        postConnection.setDoOutput(true);
        postConnection.setDoInput(true);
        OutputStreamWriter wr = new OutputStreamWriter(postConnection.getOutputStream());
        wr.write(data);
        wr.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();


        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        wr.close();
        in.close();

        final ObjectMapper mapper = new ObjectMapper();
        VirtualAccount va = mapper.readValue(response.toString(),VirtualAccount.class);
        String decodeData = hash.parseData(va.getData(),cid,key);

        converted = "{";
        converted = converted + "\"status\":"+" "+"\""+va.getStatus()+"\",";
        converted = converted + "\"data\":" + decodeData;
        converted = converted + "}";
        return converted;
    }
}
