package nr.king.familytracker.model.http.dashboardModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardRequestBody {
    private HomeModel homeModel;
    private String number;
    private String fromDate;
    private String toDate;
}
