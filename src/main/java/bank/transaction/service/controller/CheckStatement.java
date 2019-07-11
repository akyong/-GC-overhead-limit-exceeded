package bank.transaction.service.controller;

import bank.transaction.service.Common;
import bank.transaction.service.domain.AccessGrant;
import bank.transaction.service.domain.AccountStatement;
import bank.transaction.service.domain.AccountStatementDetail;
import bank.transaction.service.impl.BCAErrorHandler;
import bank.transaction.service.impl.BCATransactionInterceptor;
import bank.transaction.service.impl.BusinessBankingTemplate;
import bank.transaction.service.impl.Oauth2Template;
import bank.transaction.service.repository.Oauth2Operations;
import bank.transaction.service.repository.OrderServiceRepository;
import bank.transaction.service.service.AccountStatementService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@Controller("/cek-statement")
public class CheckStatement {
    private static Logger LOG = LoggerFactory.getLogger(CheckStatement.class);
    private final AccountStatementService accountStatementService;
    private RestTemplate restTemplate;
    private final Common common;
    private final OrderServiceRepository orderServiceRepository;

    public CheckStatement(AccountStatementService accountStatementService, RestTemplate restTemplate, Common common,OrderServiceRepository orderServiceRepository){
        this.accountStatementService = accountStatementService;
        this.restTemplate = restTemplate;
        this.orderServiceRepository = orderServiceRepository;
        this.common = common;
    }

    @Post("/")
    public String checkstatement(@NotNull int month, @NotNull int day){
        AccountStatement ac = null;
        Calendar now = Calendar.getInstance();
        List<BigDecimal> listAmount = new ArrayList<>();

        Date fromDate = toDate(2019, month,day);
        Date endDate = toDate(2019, month, day);

//        Date fromDate = toDate(year, month,day);
//        Date endDate = toDate(year, month, day);

        /** THIS IS IMPORTANT */
        BusinessBankingTemplate businessBankingTemplate = new BusinessBankingTemplate(getRestTemplate());
        try {
            ac = accountStatementService.saveConditional(businessBankingTemplate.getStatement(common.BCA_CORPORATE_ID,common.BCA_ACCOUNT_NUMBER, fromDate, endDate));
            LOG.info("RESULT -- : {}",ac.toString());
            for (AccountStatementDetail acd: ac.getAccountStatementDetailList()) {
               listAmount.add(acd.getAmount());
            }

            orderServiceRepository.CheckToTokdis(listAmount);
        }
        catch (Exception ex){
            LOG.error("----------- NO TRANSACTION!!!!");
        }

        return ac.getAccountStatementDetailList().toString();
    }

    protected RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        Oauth2Operations oauth2Operations = new Oauth2Template();
        AccessGrant accessGrant = oauth2Operations.getToken(common.BCA_CLIENT_ID, common.BCA_CLIENT_SECRET);
        restTemplate.setInterceptors(Collections.singletonList(new BCATransactionInterceptor(accessGrant.getAccessToken(), common.BCA_API_KEY, common.BCA_API_SECRET)));
        restTemplate.setErrorHandler(new BCAErrorHandler());
        return restTemplate;
    }

    protected Date toDate(int year, int month, int day) {
        Calendar calendar = new GregorianCalendar(year, month - 1, day);
        return new Date(calendar.getTimeInMillis());
    }

    public String getListAmount(List<BigDecimal> listamount){
        String id = "(";
        for(int a = 0 ; a< listamount.size(); a++){
            if(a == listamount.size()-1){
                id = id+listamount.get(a);
                id = id+")";
            }
            else{
                id = id+listamount.get(a);
                id = id+",";
            }
        }
        return id;
    }

    @Put("/update")
    public void updateAccountStatementDetail(int id){
        orderServiceRepository.updateAccountStatementDetail(id);
    }
}
