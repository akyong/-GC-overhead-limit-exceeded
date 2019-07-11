package bank.transaction.service.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/api/bni/virtual-account")
public class VirtualAccountController {

    private final VirtualAccountPost bniEncryption;
    private final VirtualAccountInquiry virtualAccountInquiry;
    private final VirtualAccountUpdate virtualAccountUpdate;
    private final NotificationEndpoint notificationEndpoint;

    protected VirtualAccountController(VirtualAccountPost bniEncryption, VirtualAccountInquiry virtualAccountInquiry,
                                       VirtualAccountUpdate virtualAccountUpdate,
                                       NotificationEndpoint notificationEndpoint){
        this.bniEncryption = bniEncryption;
        this.virtualAccountInquiry = virtualAccountInquiry;
        this.virtualAccountUpdate = virtualAccountUpdate;
        this.notificationEndpoint = notificationEndpoint;
    }

    @Post("/create")
    public String BniDev(String trx_id, String trx_amount, String billing_type, String customer_name, String customer_email, String customer_phone, String virtual_account, String datetime_expired, String description) throws Exception {
        return bniEncryption.BNI_DEV(trx_id, trx_amount,billing_type,customer_name,customer_email, customer_phone,virtual_account, datetime_expired, description);
    }

    @Post("/inquiry")
    public String Bni_Inquriy_Dev(String trx_id) throws Exception {
        return virtualAccountInquiry.BNI_INQUIRY_DEV(trx_id);
    }

    @Post("/update")
    public String Bni_Update_Dev(String trx_id, String trx_amount,String customer_name, String customer_email, String customer_phone,String datetime_expired, String description) throws Exception {
        return virtualAccountUpdate.BNI_UPDATE_DEV(trx_id, trx_amount,customer_name, customer_email, customer_phone,datetime_expired, description);
    }

    @Post("/callback")
    public String Bni_Notif(String client_id, String data) throws Exception {
        return notificationEndpoint.Bni_notif(client_id, data);
    }
}
