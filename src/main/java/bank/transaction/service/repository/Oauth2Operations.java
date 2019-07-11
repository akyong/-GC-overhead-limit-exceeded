package bank.transaction.service.repository;

import bank.transaction.service.domain.AccessGrant;

public interface Oauth2Operations {
    AccessGrant getToken(String clientId, String clientSecret);
}
