package bank.transaction.service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Singleton
public class AccessGrant {
    @NotNull
    @JsonProperty("access_token")
    private String accessToken;

    @NotNull
    @JsonProperty("token_type")
    private String tokenType;

    @NotNull
    @JsonProperty("expires_in")
    private int expiresIn;

    @NotNull
    @JsonProperty("scope")
    private String scope;


    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getAccessToken() {
        return accessToken;
    }

    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getTokenType() {
        return tokenType;
    }

    public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setScope(String scope) { this.scope = scope; }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "AccessGrant{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", scope='" + scope + '\'' +
                '}';
    }
}
