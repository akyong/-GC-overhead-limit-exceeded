package bank.transaction.service.impl;

import bank.transaction.service.scheduler.TransactionCheckerJob;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * BCA Transaction Interceptor
 * </p>
 * Will perform adding required header in order for transaction can pass through
 * Such as, adding transaction signing
 *
 * @author abc
 */


@Singleton
public class BCATransactionInterceptor implements ClientHttpRequestInterceptor {
    private String accessToken;

    private String apiKey;

    private String apiSecret;

    private static final Logger log = LoggerFactory.getLogger(BCATransactionInterceptor.class);

    public BCATransactionInterceptor(String accessToken, String apiKey, String apiSecret) {
        this.accessToken = accessToken;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        logRequest(httpRequest, body);

        String timestamp = getTimestamp();
        HttpHeaders httpHeaders = httpRequest.getHeaders();
        httpHeaders.set("Authorization", "Bearer " + accessToken);
        httpHeaders.set("X-BCA-Key", apiKey);
        httpHeaders.set("X-BCA-Signature", sign(httpRequest, timestamp, new String(body)));
        httpHeaders.set("X-BCA-Timestamp", timestamp);

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.remove("Accept-Charset");

        ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, body);
        logResponse(response);
//        LOG.info("BCA {}",  clientHttpRequestExecution.execute(httpRequest, body).

        return response;
    }

    private String sign(HttpRequest httpRequest, String timestamp, String body) {

        String text = httpRequest.getMethod().name() + ":" +
                getCompletePath(httpRequest) + ":" +
                accessToken + ":" +
                Hex.encodeHexString(sha256(body.replaceAll("\\s", ""))).toLowerCase() + ":" +
                timestamp;

        return hmacSha256(apiSecret, text);
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

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
//        if (log.isDebugEnabled()) {
            log.info("===========================request begin================================================");
            log.info("URI         : {}", request.getURI());
            log.info("Method      : {}", request.getMethod());
            log.info("Headers     : {}", request.getHeaders());
            log.info("Request body: {}", new String(body, "UTF-8"));
            log.info("==========================request end================================================");
//        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
//        if (log.isDebugEnabled()) {
            log.info("============================response begin==========================================");
            log.info("Status code  : {}", response.getStatusCode());
            log.info("Status text  : {}", response.getStatusText());
            log.info("Headers      : {}", response.getHeaders());
            log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            log.info("=======================response end=================================================");
//        }
    }
}
