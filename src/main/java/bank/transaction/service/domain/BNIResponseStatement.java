package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;
import java.util.List;
@Singleton
public class BNIResponseStatement {

    @JsonProperty("cliendId")
    private String cliendId;

    @JsonProperty("parameters")
    private BNIParameter bniParameter;

    private BNIStatement bniStatement;

    public void setCliendId(String cliendId) { this.cliendId = cliendId; }
    public String getCliendId() { return cliendId; }

    public void setBniParameter(BNIParameter bniParameter) { this.bniParameter = bniParameter; }
    public BNIParameter getBniParameter() { return bniParameter; }

    public void setBniStatement(BNIStatement bniStatement) { this.bniStatement = bniStatement; }
    public BNIStatement getBniStatement() { return bniStatement; }

    @Override
    public String toString() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
