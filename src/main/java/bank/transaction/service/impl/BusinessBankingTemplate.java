
package bank.transaction.service.impl;

import bank.transaction.service.domain.*;
import bank.transaction.service.scheduler.TransactionCheckerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.*;

@Singleton
public class BusinessBankingTemplate extends AbstractBCAOperations implements BusinessBankingOperations {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessBankingTemplate.class);
    public BusinessBankingTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    @Override
//    public AccountBalance getBalance(String corporateId, String ... accountNo) {
//
//        List<String> pathVariables = new ArrayList<>();
//        pathVariables.add(corporateId);
//        pathVariables.add(StringUtils.arrayToCommaDelimitedString(accountNo));
//
//        return restTemplate.getForObject(buildUrl("/banking/v2/corporates/{}/accounts/{}", pathVariables), AccountBalance.class);
//    }

    @Override
    public AccountStatement getStatement(String corporateId, String accountNo, Date startDate, Date endDate) {

        List<String> pathVariables = new ArrayList<>();
        pathVariables.add(corporateId);
        pathVariables.add(accountNo);

        Map<String, Object> requestParam = new HashMap<>();
        requestParam.put("StartDate", formatDate(startDate));
        requestParam.put("EndDate", formatDate(endDate));

        return restTemplate.getForObject(buildUrl("/banking/v3/corporates/{}/accounts/{}/statements", pathVariables, requestParam), AccountStatement.class);
    }

//    @Override
//    public InhouseTransferResponse inhouseTransfer(InhouseTransferRequest inhouseTransferRequest) {
//
//        return restTemplate.postForObject(buildUrl("/banking/corporates/transfers"), inhouseTransferRequest, InhouseTransferResponse.class);
//    }
//

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

}
