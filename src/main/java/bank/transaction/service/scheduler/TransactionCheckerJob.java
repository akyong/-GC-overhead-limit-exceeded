package bank.transaction.service.scheduler;

import bank.transaction.service.Common;
import bank.transaction.service.impl.*;
import bank.transaction.service.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.*;
import javax.inject.Singleton;
import io.micronaut.scheduling.annotation.Scheduled;
import bank.transaction.service.domain.AccessGrant;
import bank.transaction.service.domain.AccountStatement;
import bank.transaction.service.domain.AccountStatementDetail;
import bank.transaction.service.service.BcaService;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Singleton
public class TransactionCheckerJob {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionCheckerJob.class);
    private final BcaService bcaService;
    private final Oauth2Template oauth2Template;
    private final Common common;
    private final AccountStatementRepository accountStatementRepository;
    private final OrderServiceRepository orderServiceRepository;
    private final Oauth2OperationsBNI oauth2OperationsBNI;
    private final ExpeditionRepository expeditionRepository;
    private final BNIBankingTemplate bniBankingTemplate;
    protected AccessGrant accessGrant;
    private BusinessBankingTemplate businessBankingTemplate;
    private RestTemplate restTemplate;



    public TransactionCheckerJob(Common common, BNIBankingTemplate bniBankingTemplate,ExpeditionRepository expeditionRepository, OrderServiceRepository orderServiceRepository, BcaService bcaService, Oauth2Template oauth2Template, Oauth2OperationsBNI oauth2OperationsBNI, RestTemplate restTemplate, AccountStatementRepository accountStatementRepository){
        this.bcaService = bcaService;
        this.oauth2Template = oauth2Template;
        this.restTemplate = restTemplate;
        this.accountStatementRepository = accountStatementRepository;
        this.oauth2OperationsBNI = oauth2OperationsBNI;
        this.orderServiceRepository = orderServiceRepository;
        this.expeditionRepository = expeditionRepository;
        this.bniBankingTemplate = bniBankingTemplate;
        this.common = common;
    }

    /**
     * Case No. 1 Reseller ->lebih dari 6 jam batas pembayaran
     * TODO Auto Check jika pembayaran expired -> if payment_status = 0 and payment_expired_at < now()
     * then payment_status = 0 and payment_verified_by = 0 and payment_verified_at = now() and is_paid = 0 and is_cancelled = 1
     * Then update stock
     *
     * */
    @Scheduled(fixedDelay = "270s", initialDelay = "5s")
    void expiredPaymentChecking(){
        orderServiceRepository.autoUpdatePaymentStatusIfExpired();
    }

    /**
     * Case No. 2 -> Reseller sudah melakukan pembayaran -> Approve akan otomatsi berjalan dan transaksi akan otomatis diteruskan ke supplier
     * TODO update order sumarries ->payment_status = 1, payment_verified_by = 0, payment_verified_at = now(), is_paid = 1
     * TODO update order suppliers ->supplier_feedback_expired_AT = now()+1 DAY, order_status = 1
     * TODO Send NotificationSupplier ONESIGNAL "Pesanan Berhasil dibayar" -> "Pembayaran pesananmu TDO/20190225/0000160 telah dikonfirmasi dan diteruskan ke penjual. Silahkan tunggu pesanan dikirim."
     * */
    @Scheduled(fixedDelay = "270s", initialDelay = "35s")
    void executeEveryTen() throws Exception {

        List<BigDecimal> listAmount = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);

//        Date fromDate = toDate(2016, 9,1);
//        Date endDate = toDate(2016, 9, 1);

        Date fromDate = toDate(year, month,day);
        Date endDate = toDate(year, month, day);
        AccountStatement ac = null;

        LOG.info("Year : {}",year);
        LOG.info("month : {}",month);
        LOG.info("day : {}",day);
        LOG.info("From Date = {}",fromDate);
        LOG.info("From endDate = {}",endDate);

        /** THIS IS IMPORTANT */
        try {
            businessBankingTemplate = new BusinessBankingTemplate(getRestTemplate());
            ac = accountStatementRepository.saveConditional(businessBankingTemplate.getStatement(common.BCA_CORPORATE_ID,common.BCA_ACCOUNT_NUMBER, fromDate, endDate));
            for (AccountStatementDetail acd: ac.getAccountStatementDetailList()) {
                listAmount.add(acd.getAmount());
            }
            orderServiceRepository.CheckToTokdis(listAmount);
        }
        catch (Exception ex){
            LOG.error("----------- LOG HIT BCA :  {}",ex);
            ex.printStackTrace();
            LOG.error("----------- NO TRANSACTION!!!!");
        }
    }

    /**
     * Case No. 3 Reseller -> Dikirim -> Pesanan kaan otomatis pindah ke transaksi sampai
     * CronJob berjalan setiap 4 jam sekali
     * TODO update order supplier -> delivery_status = 1 , order_status = 5 and confirmed_expired_at = now()+2 DAYS and is_delivered = 1 and delivered_at now()
     * TODO Send NotificationSupplier ONESIGNAL -> Pesanan Sampai -> "Pesananmu INV/20190225/00000005 telah sampai. Silahkan konfirmasi penerimaan pesananmu."
     * */
    @Scheduled(fixedDelay = "14400s", initialDelay = "60s")
    void executeEveryFourtyFive() throws Exception {
        expeditionRepository.CheckTracking();
    }

    /**
     * Case No. 4 Reseller-> "Reseller - Sampai" ->Pesanan akan otomatis pindah ke transaksi selesai
     * TODO Autocheck if confirmed_expired_at < now() and confirmed_at = null
     * TODO send ONESIGNAL notification -> Pesanan Selesai -> "Pesananmu INV/20190225/00000005 telah selesai. Silahkan berikan penilaian pesananmu."
     * then update order_status = 6 and confirmed_at now()
     * + update saldo ke beranda supplier
     * */
    @Scheduled(fixedDelay = "270s", initialDelay = "90s")
    void executeUpdateTransactionDone(){
        orderServiceRepository.updateOrderStatusToDone();
    }

    /**
     * CASE No. 1 Supplier -> Supplier -> Transaksi - Pesanan BAru jika Supplier tidak merespon pesanan tersebut melebihi 1x24jam
     * TODO autocheck if supplier_feeback_expired_at < now() and supplier_feedback_at = null and order_status = 1
     * then update is_rejected = 1 and order_status = 2 and supplier_feedback_at = now()
     * - balikin stock
     * - balikin saldo ke brankas
     * */
    @Scheduled(fixedDelay = "270s", initialDelay = "120s")
    void executeUpdateStatusTransactionIfSupplierNotRespond(){
        orderServiceRepository.updateOrderStatusRejected();
    }

    /**
     * Case NocheckForReminder.2 -> Supplier-> Pesanan akan otomatis dibatalkan, Supplier tidak meingin pesanan lebih dari 2x24Jam
     * TODO update order summaries -> is_rejected = 1 and order_status = 2 pesanan ditolak
     * - Balikin stock
     * - balikin saldo reseller brankas
     * */
    @Scheduled(fixedDelay = "270s", initialDelay = "150s")
    void executeUpdateStatusTransactionIfSupplierNotSentTheOrder(){
        orderServiceRepository.UpdateIsRejectedIfSupplierNotSentTheOrder();
    }

//    /**
//     * Case Reminder
//     * TODO Notifikasi Pesanan Menunggu Pembayaran
//     * TODO Send NotificationSupplier ONESIGNAL -> "Segera lakukan pembayaran sebesar Rp 1.234.000 untuk pesananmu TDO/20190225/0000160 sebelum 06-02-2019 22.32 untuk menghindari pembatalan."
//     * */
//    @Scheduled(fixedDelay = "270s", initialDelay = "180s")
//    void executeForReminder(){
//        orderServiceRepository.checkForReminder();
//    }

    /**
     * TODO Case Supplier - ONESIGNAL
     * TODO "notifikasi segera kirim barang, input no. Resi"
     * */
    @Scheduled(fixedDelay = "270s", initialDelay = "210s")
    void executeNotificationMustSendItem(){
        orderServiceRepository.sentNotifMustSentItem();
    }

    protected RestTemplate getRestTemplate() {
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        RestTemplate restTemplate = new RestTemplate(factory);
        Oauth2Operations oauth2Operations = new Oauth2Template();
        AccessGrant accessGrant = oauth2Operations.getToken(common.BCA_CLIENT_ID, common.BCA_CLIENT_SECRET);
//        BCATransactionInterceptor bca = new BCATransactionInterceptor(accessGrant.getAccessToken(), common.BCA_API_KEY, common.BCA_API_SECRET);

        restTemplate.setInterceptors(Collections.singletonList(new BCATransactionInterceptor(accessGrant.getAccessToken(), common.BCA_API_KEY, common.BCA_API_SECRET)));
        restTemplate.setErrorHandler(new BCAErrorHandler());
        return restTemplate;
    }

    protected Date toDate(int year, int month, int day) {
        Calendar calendar = new GregorianCalendar(year, month - 1, day);
        return new Date(calendar.getTimeInMillis());
    }
}
