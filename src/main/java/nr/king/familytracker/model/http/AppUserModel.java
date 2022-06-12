package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public  class AppUserModel implements Serializable {
    private Object trialStartDate;
    private String createdDate;
    private String mobilePhone;
    private String trialEndDate;
    private List<Followings> followings;
    private String subscribeStatus;
    private Integer maxFollowCount;
    private Object pushToken;
    private String fireBaseId;

}