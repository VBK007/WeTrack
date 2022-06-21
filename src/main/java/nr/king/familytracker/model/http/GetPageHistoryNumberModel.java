package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetPageHistoryNumberModel {
    Integer pageLimit= 1;
    String phoneNumber;
    Integer start= 0;
    private HomeModel homeModel;
}
