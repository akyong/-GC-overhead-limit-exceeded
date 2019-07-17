package bank.transaction.service.controller;

import bank.transaction.service.repository.OrderServiceRepository;
import bank.transaction.service.service.OrderService;
import bank.transaction.service.validation.ReceivePaymentValidation;
import bank.transaction.service.validation.ReceivedPaymentVIrfan;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/api/receive-payment")
public class ReceivePaymentController {
    private OrderServiceRepository orderServiceRepository;
    private Logger Log = LoggerFactory.getLogger(ReceivePaymentController.class);
    public ReceivePaymentController(OrderServiceRepository orderServiceRepository){
        this.orderServiceRepository = orderServiceRepository;
    }


    /**
     * @param receivePaymentValidation
     * return String
     * @apiNote this api is using by elka
     * */
    @Post("/")
    public String index(@Body ReceivePaymentValidation receivePaymentValidation){
        return orderServiceRepository.COMPLETE_TRX(receivePaymentValidation.getInvoiceId());
    }

    /**
     * @param receivedPaymentVIrfan
     * return String
     * @apiNote this api is using by irfan for BNI
     * */
    @Post("/")
    public String index2(@Body ReceivedPaymentVIrfan receivedPaymentVIrfan){
        return "";
//        return orderServiceRepository.COMPLETE_TRX(receivedPaymentVIrfan.getInvoiceNo());
    }

    @Post("/test")
    public String index22(){
        return "test";
//        return orderServiceRepository.COMPLETE_TRX(receivedPaymentVIrfan.getInvoiceNo());
    }
}
