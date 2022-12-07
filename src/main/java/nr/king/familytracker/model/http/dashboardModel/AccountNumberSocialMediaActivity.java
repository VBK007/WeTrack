package nr.king.familytracker.model.http.dashboardModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountNumberSocialMediaActivity {
    private int totalNumberOfOnline;
    private int totalNumberOfOffline;
    private int totalNumberOfHours;

    private int  maxNumber;
}
