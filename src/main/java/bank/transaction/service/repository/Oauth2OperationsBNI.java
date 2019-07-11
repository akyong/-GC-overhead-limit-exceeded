package bank.transaction.service.repository;

import bank.transaction.service.domain.AccessGrant;

public interface Oauth2OperationsBNI {
    AccessGrant getToken(String clientId, String clientSecret);
}
