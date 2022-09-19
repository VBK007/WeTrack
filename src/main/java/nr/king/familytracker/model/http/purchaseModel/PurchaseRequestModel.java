package nr.king.familytracker.model.http.purchaseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseRequestModel {
    private String userId;
    private String  purchaseMode;
    private String purcasePlatform;
    private String country;
    private String amount;
    private String transactionId;
    private String transactionRemarks;
    private String expiryDate;
    private String createdAt;
    private String expiryAt;
    private String packageName;
    private HomeModel homeModel;
}
