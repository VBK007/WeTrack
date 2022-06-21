package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.GetPhoneNumberHistoryModel;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendHistorystatusToAppModel {
    private Boolean status;
    private String message;
    private ArrayList<GetPhoneNumberHistoryModel> statusList;

}
