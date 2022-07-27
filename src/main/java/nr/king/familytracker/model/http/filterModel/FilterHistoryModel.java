package nr.king.familytracker.model.http.filterModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.MainHomeUserModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterHistoryModel {
    private String endDate = "";
    private Integer endHour;
    private Integer pageLimit;
    private String phoneNumber = "";
    private Integer start;
    private String startDate = "";
    private Integer startHour;
    private Float timeZone;
    private HomeModel homeModel;
}
