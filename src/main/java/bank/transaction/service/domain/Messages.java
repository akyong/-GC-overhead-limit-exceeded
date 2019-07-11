package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;

@Singleton
public class Messages {
    @JsonProperty("Indonesian")
    private String indonesian;

    @JsonProperty("English")
    private String english;

    public String getIndonesian() {
        return indonesian;
    }

    public String getEnglish() {
        return english;
    }

    @Override
    public String toString() {
        return "Messages{" +
                "indonesian='" + indonesian + '\'' +
                ", english='" + english + '\'' +
                '}';
    }
}
