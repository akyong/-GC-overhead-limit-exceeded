package bank.transaction.service.impl;

import bank.transaction.service.Common;
import bank.transaction.service.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;
import java.nio.charset.Charset;
import java.util.*;


@Singleton
public class BNIBankingTemplate extends AbstractBNIOperations implements BNIBankingOperations {
    private static final Logger LOG = LoggerFactory.getLogger(BNIBankingTemplate.class);
    private final Common common;
    public BNIBankingTemplate(RestTemplate restTemplate, Common common) {
        this.restTemplate = restTemplate;
        this.common = common;
    }

    @Override
    public BNIStatement getPaymentStatus(String customerReferenceNumber, AccessGrant accessGrant) {
        String signature = "";
        Map<String, Object> requestParam = new HashMap<>();
        requestParam.put(common.ACCESS_TOKEN, accessGrant.getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(common.BNI_HEADER_X_API_KEY, common.BNI_API_KEY);//API Keys

        BNISignatureHeader headerJWT = new BNISignatureHeader();
        headerJWT.setAlgoritma(common.ALGORITM_HS256);
        headerJWT.setType(common.FORMAT_TYPE);
        final String encodeHeaderJWT = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJWT.JSONWithOutSpace().getBytes());

        BNISignaturePayload payloadJWT = new BNISignaturePayload();
        payloadJWT.setClientId(common.BNI_CLIENT_ID);
        payloadJWT.setCustomerReferenceNumber(customerReferenceNumber);

        final String encodePayloadJWT = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJWT.JSONWithOutSpace().getBytes());

        try {
           signature = encodeSignature(common.BNI_API_SECRET,encodeHeaderJWT+"."+encodePayloadJWT);//API Secret Key, header.payload
        } catch (Exception e) {
            e.printStackTrace();
        }
        String resultSign = encodeHeaderJWT+"."+encodePayloadJWT+"."+signature;
        BNIBodyRequestForPaymentStatus body = new BNIBodyRequestForPaymentStatus(common.BNI_CLIENT_ID, resultSign, customerReferenceNumber);
        HttpEntity<String> request = new HttpEntity<>(body.toString().replaceAll("\n","").replaceAll(" ",""), headers);
        return restTemplate.postForObject(buildUrl(common.BNI_PATH_GET_PAYMENT_STATUS, null, requestParam),request, BNIStatement.class);
    }

    private String encodeSignature(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance(common.HMAC_SHA256);
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(Charset.defaultCharset()), common.HMAC_SHA256);
        sha256_HMAC.init(secret_key);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256_HMAC.doFinal(data.getBytes()));
    }
}
