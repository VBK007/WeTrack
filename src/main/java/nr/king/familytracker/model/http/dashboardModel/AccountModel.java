package nr.king.familytracker.model.http.dashboardModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountModel {
    private String createdAt;
    private long trackingTime;
    private String expiryAt;
    private boolean showAdd;
    private String showExpiryDate;
    private boolean isTracking;
    private AccountNumbers accountNumbers;
    private AccountNumberSocialMediaActivity accountNumberSocialMediaActivity;
    private String purchaseMode;

    private String appVersion;
    private boolean isForceUpdate;

}
