package bank.transaction.service.impl;

import java.util.List;
import java.util.Map;

import bank.transaction.service.Common;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import javax.inject.Singleton;

@Singleton
public class AbstractBCAOperations {
    /**
     * BCA Sandbox URL
     */

//    private static final String BCA_API_SANDBOX_URL = "https://sandbox.bca.co.id";
    private static final String BCA_API_SANDBOX_URL = "https://api.klikbca.com";

//    RestTemplateA restTemplate = BeanContext.run().getBean(RestTemplateA.class);
    protected RestTemplate restTemplate ;

    final String buildUrl(String path) {

        return buildUrl(path, null, null);
    }

    final String buildUrl(String path, Map<String, Object> requestParam) {

        return buildUrl(path, null, requestParam);
    }

    final String buildUrl(String path, List<String> pathVariables) {

        return buildUrl(path, pathVariables, null);
    }

    final String buildUrl(String path, List<String> pathVariables, Map<String, Object> requestParam) {

        if (pathVariables != null) {

            for (String pathVariable : pathVariables) {
                path = path.replaceFirst("\\{}", pathVariable);
            }
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BCA_API_SANDBOX_URL).path(path);

        if (requestParam != null) {

            requestParam.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x -> builder.queryParam(x.getKey(), x.getValue()));
        }

        return builder.build().toUriString();
    }
}
