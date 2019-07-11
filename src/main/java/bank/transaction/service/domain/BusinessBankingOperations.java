package bank.transaction.service.domain;

import javax.inject.Singleton;
import java.util.Date;

@Singleton
public interface BusinessBankingOperations {
//    AccountBalance getBalance(String corporateId, String ... accountNo);

    AccountStatement getStatement(String corporateId, String accountNo, Date startDate, Date endDate);

//    InhouseTransferResponse inhouseTransfer(InhouseTransferRequest inhouseTransferRequest);

}
