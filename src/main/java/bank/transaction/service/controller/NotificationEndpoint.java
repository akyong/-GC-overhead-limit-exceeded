package bank.transaction.service.controller;

import bank.transaction.service.BniEncryptionClass;
import bank.transaction.service.domain.virtualaccount.TransactionPaymentNotification;
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class NotificationEndpoint {

    @PersistenceContext
    private EntityManager entityManager;

    public NotificationEndpoint(@CurrentSession EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Transactional
    public String Bni_notif(String client_id, String data) throws Exception{

        BniEncryptionClass hash = new BniEncryptionClass();
        String cid = "00629";
        String key = "b04964206026f28683749f7d36afe871";
        String dataBNI = "{\"client_id\":\""+client_id+"\",\"data\":\""+data+"\"}";
        String converted = "";

        String decodeData = hash.parseData(data,cid,key);

        TransactionPaymentNotification tf = new TransactionPaymentNotification(decodeData);
        entityManager.persist(tf);

        converted = "{";
        converted = converted + "\"status\":"+" "+"\"000\"";
        converted = converted + "}";
        return converted;
    }
}
