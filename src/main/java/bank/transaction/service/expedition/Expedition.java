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
package bank.transaction.service.expedition;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class Expedition {
    public Expedition(){ }

    public Expedition(String code, String codeMessage, String codeType, List<ExpeditionDetail> expeditionDetailList){
        this.code = code;
        this.codeMessage = codeMessage;
        this.codeType = codeType;
        this.expeditionDetailList = expeditionDetailList;
    }

    @JsonProperty("code")
    private String code;

    @JsonProperty("code_message")
    private String codeMessage;

    @JsonProperty("code_type")
    private String codeType;

    @JsonProperty("is_delivered")
    private boolean isDelivered;

    @JsonProperty("data")
    private List<ExpeditionDetail> expeditionDetailList;

    public void setCode(String code) { this.code = code; }
    public String getCode() { return code; }

    public void setCodeMessage(String codeMessage) { this.codeMessage = codeMessage; }
    public String getCodeMessage() { return codeMessage; }

    public void setCodeType(String codeType) { this.codeType = codeType; }
    public String getCodeType() { return codeType; }

    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    public boolean getDelivered(){return isDelivered; }

    public void setExpeditionDetailList(List<ExpeditionDetail> expeditionDetailList) { this.expeditionDetailList = expeditionDetailList; }
    public List<ExpeditionDetail> getExpeditionDetailList() { return expeditionDetailList; }

    @Override
    public String toString() {
        return "Expedition{" +
                "code='" + code + '\'' +
                ", codeMessage='" + codeMessage + '\'' +
                ", codeType='" + codeType +'\'' +
                ", isDelivered=" + isDelivered +
                ", data='" + expeditionDetailList + '\'' +
                '}';
    }
}
