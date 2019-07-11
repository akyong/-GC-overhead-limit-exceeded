package bank.transaction.service.impl;

import bank.transaction.service.domain.BCAException;
import bank.transaction.service.domain.ErrorWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class BCAErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        ErrorWrapper errorWrapper = extractErrorFromResponse(clientHttpResponse);
        throw new BCAException(errorWrapper.getErrorCode(), errorWrapper.getMessages());
    }

    private ErrorWrapper extractErrorFromResponse(ClientHttpResponse clientHttpResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(clientHttpResponse.getBody(), ErrorWrapper.class);
    }
}
