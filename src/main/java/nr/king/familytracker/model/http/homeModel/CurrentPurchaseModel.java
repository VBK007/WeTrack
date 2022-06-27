package nr.king.familytracker.model.http.homeModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentPurchaseModel {
    private String Expiry_time;
    private String purchaseMode;
    private int maxNumber;
}
