package bank.transaction.service.domain;

import javax.inject.Singleton;
import java.util.Date;

@Singleton
public interface BNIBankingOperations {
    BNIStatement getPaymentStatus(String customerReferenceNumber,  AccessGrant accessGrant);
}
