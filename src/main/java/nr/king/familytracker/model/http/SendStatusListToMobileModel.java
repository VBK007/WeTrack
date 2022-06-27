package nr.king.familytracker.model.http;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.CurrentPurchaseModel;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendStatusListToMobileModel {
    private CurrentPurchaseModel currentPurchaseModel;
    private ArrayList<SendHistorystatusToAppModel> sendHistorystatusToAppModelArrayList;
}
