package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class AccountError {
    @JsonProperty("AccountNumber")
    private String no;

    public String getNo() {
        return no;
    }

    @Override
    public String toString() {
        return "AccountError{" +
                super.toString() +
                "no='" + no + '\'' +
                '}';
    }
}
