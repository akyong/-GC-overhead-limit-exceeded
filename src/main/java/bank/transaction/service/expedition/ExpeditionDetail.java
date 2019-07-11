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

@Singleton
public class ExpeditionDetail {

    public ExpeditionDetail(){

    }

    public ExpeditionDetail(String location, String remark, String status, String time, Expedition expedition){
        this.expedition = expedition;
        this.location = location;
        this.remark = remark;
        this.status = status;
        this.time = time;
    }

    @JsonProperty("location")
    private String location;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("status")
    private String status;

    @JsonProperty("time")
    private String time;

    private Expedition expedition;

    public void setLocation(String location) { this.location = location; }
    public String getLocation() { return location; }

    public void setRemark(String remark) { this.remark = remark; }
    public String getRemark() { return remark; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setTime(String time) { this.time = time; }
    public String getTime() { return time; }

    public void setExpedition(Expedition expedition) { this.expedition = expedition; }
    public Expedition getExpedition() { return expedition; }

    @Override
    public String toString() {
        return "ExpeditionDetail{" +
                "location='" + location + '\'' +
                ", remark='" + remark + '\'' +
                ", status='" + status +'\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
