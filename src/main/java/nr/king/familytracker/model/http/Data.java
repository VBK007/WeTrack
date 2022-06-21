package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    private String trialStartDate;
    private String createdDate;
    private String mobilePhone;
    private String trialEndDate;
    private List<Followings> followings;
    private String subscribeStatus;
    private Integer maxFollowCount;
    private String pushToken;
    private String fireBaseId;
}
