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

import bank.transaction.service.domain.AccessGrant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Base64;
import io.micronaut.http.MediaType;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class BcaService {
    private static final Logger LOG = LoggerFactory.getLogger(BcaService.class);
    private final String URL_BCA = "https://sandbox.bca.co.id";
    private final String API_KEY = "a4a5403b-a49e-48e6-9213-c72ccfcf54eb";
    private final String API_SECRET = "70ead5e3-15e1-431f-9774-a4d4efe9ba16";
    private final String CLIENT_ID = "e571d7fd-9b5b-46e8-a835-2f5793c44611";
    private final String CLIENT_SECRET = "cb711807-bd70-4069-a6ce-bb9739716cae";
    private final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private final String CORPORATE_ID = "BCAAPI2016";
    private final String ACCOUNT_NUMBER = "0201245680";
    protected AccessGrant accessGrant;

    public BcaService(AccessGrant accessGrant){
        this.accessGrant = accessGrant;
    }

    public AccessGrant send() throws Exception {
        String resultEncodeBase64 = encodeBase64(CLIENT_ID,CLIENT_SECRET);
        LOG.info("encode base 64 with Basic = {} ", resultEncodeBase64);
        URL obj = new URL(URL_BCA+"/api/oauth/token");
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", resultEncodeBase64);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        String urlParameters = "grant_type=client_credentials";

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        accessGrant = new ObjectMapper().readValue(response.toString(), AccessGrant.class);
        return accessGrant;

    }

    public String getTransaction(String startDate, String endDate) throws Exception {
        startDate="2016-09-01";
        endDate="2016-09-01";
        String url = "/banking/v3/corporates/"+CORPORATE_ID+"/accounts/"+ACCOUNT_NUMBER+"/statements?StartDate="+startDate+"&EndDate="+endDate;
        String timestamp = getTimestamp();
        url = URL_BCA+url;
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        connection.setRequestMethod("GET");

        LOG.info("\nAmbil kembali Access Token = {}", accessGrant.getAccessToken());
        //add request header
        connection.setRequestProperty("Authorization", "Bearer "+accessGrant.getAccessToken());
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
        connection.setRequestProperty("Origin", "tokodistributor.com");
        connection.setRequestProperty("X-BCA-Key", API_KEY);
        LOG.info("\nAPI KEY= {}", API_KEY);
        connection.setRequestProperty("X-BCA-Timestamp", timestamp);
        LOG.info("\nTIMESTAMP= {}", timestamp);

        LOG.info("\nTIMESTAMP= {}", sign(url, timestamp, ""));
        connection.setRequestProperty("X-BCA-Signature", sign(url, timestamp, ""));

        int responseCode = connection.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

        LOG.info("\nPRINT RESULT = {}", response.toString());
        return null;
    }

    private String encodeBase64(@NotNull String clientId, @NotNull String clientSecret){
        Base64 encode = Base64.encode(clientId+":"+clientSecret);
        LOG.info("Hasil encode : {}", encode);
        return "Basic "+Base64.encode(clientId+":"+clientSecret).toString();
    }

    private String sign(String url, String timestamp, String body) {
        String text = "GET" + ":" + url + ":" +
                accessGrant.getAccessToken() + ":" +
                Hex.encodeHexString(sha256(body.replaceAll("\\s", ""))).toLowerCase() + ":" +
                timestamp;

        return hmacSha256(API_SECRET, text);
    }

    /**
     * Generate complete path with the request parameter if available
     *
     * @param httpRequest HttpRequest
     * @return Path
     */
    private String getCompletePath(HttpRequest httpRequest) {

        StringBuilder builder = new StringBuilder();
        builder.append(httpRequest.getURI().getPath());
        if (httpRequest.getURI().getQuery() != null) {

            builder.append("?");
            builder.append(httpRequest.getURI().getQuery());
        }

        return builder.toString();
    }

    /**
     * Hash value using SHA-256 algorithm
     *
     * @param value plain text value
     * @return hashed value
     */
    private byte[] sha256(String value) {

        return DigestUtils.sha256(value.getBytes());
    }

    /**
     * Hash value using HMAC SHA-256 algorithm
     *
     * @param apiSecret BCA API Key Secret
     * @param value plain text value
     *
     * @return BCA Signature
     */
    private String hmacSha256(String apiSecret, String value) {

        byte[] result = HmacUtils.hmacSha256(apiSecret, value);
        return new String(Hex.encodeHex(result));
    }

    /**
     * Generate BCA Timestamp base on ISO 8601 Format
     *
     * @return Timestamp
     */
    private String getTimestamp() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return dateFormat.format(new Date());
    }

}
