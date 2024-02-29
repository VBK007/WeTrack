package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Data  implements Serializable {
    private String trialStartDate;
    private String createdDate;
    private String mobilePhone;
    private String trialEndDate;
    private List<Followings> followings;
    private String subscribeStatus;
    private Integer maxFollowCount;
    private String pushToken;
    private String fireBaseId;
    /*private boolean isQrTracking;
    private boolean isQrSessionConnected;*/
    public boolean isQrTracking;
    public boolean isQrSessionConnected;
}