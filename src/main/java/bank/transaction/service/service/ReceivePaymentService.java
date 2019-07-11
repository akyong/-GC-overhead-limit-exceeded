//package bank.transaction.service.service;
//
//import bank.transaction.service.Common;
//import bank.transaction.service.domain.ResponseModel;
//import bank.transaction.service.repository.OrderServiceRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Inject;
//import javax.inject.Named;
//import javax.inject.Singleton;
//import javax.sql.DataSource;
//import javax.validation.constraints.NotNull;
//import java.util.List;
//
//@Singleton
//public class ReceivePaymentService extends OrderService implements OrderServiceRepository {
//    @Inject
//    @Named("tokdis")
//    DataSource dataSource;
//
//    @Inject
//    @Named("maintokdis")
//    DataSource dataSourceTokdisdev;
//
//    private Logger Log = LoggerFactory.getLogger(ReceivePaymentService.class);
//    public ReceivePaymentService(Common common, NotificationService notificationService){
//        super(notificationService,common);
//    }
//
//    /**
//     * TODO Complete all task to send notification and update status
//     * @void COMPLETE_TRX
//     * @param idList as List Integer
//     *
//     * */
//    @Override
//    public String COMPLETE_TRX(@NotNull List<Integer> idList){
//        ResponseModel response = new ResponseModel();
//        try {
//            idList.forEach(it->{
//                updateToTokdis(it);
//                updateOrderSuppliers(it);
//            });
//
//            HIT_API_TO_SERVER_AFTER_GET_STATEMENT(idList);
//
//            response.setCode(200);
//            response.setCodeMessage("Success");
//            response.setCodeType("success");
//            response.setData("{}");
//        }
//        catch (Exception ex){
//            response.setCode(400);
//            response.setCodeMessage("Failed");
//            response.setCodeType("failed");
//            response.setData("{}");
//            ex.printStackTrace();
//        }
//
//        return response.toString();
//
//    }
//}
