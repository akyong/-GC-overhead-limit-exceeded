package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class AccountBalance {
    @JsonProperty("AccountDetailDataSuccess")
    private List<Account> accountList;

    @JsonProperty("AccountDetailDataFailed")
    private List<AccountError> accountErrorList;

    public List<Account> getAccountList() {
        return accountList;
    }

    public List<AccountError> getAccountErrorList() {
        return accountErrorList;
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "accountList=" + accountList +
                ", accountErrorList=" + accountErrorList +
                '}';
    }
}
