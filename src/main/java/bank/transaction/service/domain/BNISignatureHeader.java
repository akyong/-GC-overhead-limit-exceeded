package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class BNISignatureHeader {
    @JsonProperty("alg")
    private String algoritma;

    @JsonProperty("typ")
    private String type;

    public void setAlgoritma(String algoritma) {
        this.algoritma = algoritma;
    }

    public String getAlgoritma() {
        return algoritma;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String JSONWithOutSpace(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"alg\":\""+algoritma+"\"");
        sb.append(",\"typ\":\""+type+"\"");
        sb.append("}");

        return sb.toString();
    }
}

