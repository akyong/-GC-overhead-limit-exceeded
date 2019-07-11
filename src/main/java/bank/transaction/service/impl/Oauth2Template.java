/**
 * Copyright (c) 2019. PT. Distributor Indonesia Unggul. All rights reserverd.
 *
 * This source code is an unpublished work and the use of  a copyright  notice
 * does not imply otherwise. This source  code  contains  confidential,  trade
 * secret material of PT. Distributor Indonesia Unggul.
 * Any attempt or participation in deciphering, decoding, reverse  engineering
 * or in any way altering the source code is strictly  prohibited, unless  the
 * prior  written consent of Distributor Indonesia Unggul. is obtained.
 *
 * Unless  required  by  applicable  law  or  agreed  to  in writing, software
 * distributed under the License is distributed on an "AS IS"  BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or  implied.  See  the
 * License for the specific  language  governing  permissions  and limitations
 * under the License.
 *
 * Author : Bobby
 */

package bank.transaction.service.impl;

import bank.transaction.service.domain.AccessGrant;
import bank.transaction.service.repository.Oauth2Operations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Singleton;
import java.util.Base64;

@Singleton
public class Oauth2Template extends AbstractBCAOperations implements Oauth2Operations {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2Template.class);
    public Oauth2Template() {
        this.restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new BCAErrorHandler());
//        this.restTemplate = restTemplate;
    }

    @Override
    public AccessGrant getToken(String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", getAuthorization(clientId, clientSecret));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(buildUrl("/api/oauth/token"),request, AccessGrant.class);
    }

    private String getAuthorization(String clientId, String clientSecret) {

        return "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
    }


}
