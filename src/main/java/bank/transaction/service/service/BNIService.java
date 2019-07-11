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
package bank.transaction.service.service;

import bank.transaction.service.domain.AccessGrant;
import bank.transaction.service.repository.Oauth2OperationsBNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class BNIService {
    private static final Logger LOG = LoggerFactory.getLogger(BNIService.class);
    private final String URL_BCA = "https://digitalservices.bni.co.idd";
//    private final String API_KEY = "a4a5403b-a49e-48e6-9213-c72ccfcf54eb";
//    private final String API_SECRET = "70ead5e3-15e1-431f-9774-a4d4efe9ba16";
    private final String CLIENT_ID = "d78e500c-76c1-49e8-a4d8-41c5154b150e";
    private final String CLIENT_SECRET = "ad0882f2-b9b4-46c2-beca-ff2946e4e1aa";
    private final String CONTENT_TYPE = "application/x-www-form-urlencoded";
//    private final String CORPORATE_ID = "BCAAPI2016";
//    private final String ACCOUNT_NUMBER = "0201245680";

    public BNIService(){

    }


}
