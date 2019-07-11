package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class ErrorWrapper {
    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ErrorMessage")
    private Messages messages;

    public String getErrorCode() {
        return errorCode;
    }

    public Messages getMessages() {
        return messages;
    }

    @Override
    public String toString() {

        return "ErrorWrapper{" +
                "errorCode='" + errorCode + '\'' +
                ", messages=" + messages +
                '}';
    }
}
