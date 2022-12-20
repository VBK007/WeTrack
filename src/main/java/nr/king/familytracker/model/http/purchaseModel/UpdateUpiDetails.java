package nr.king.familytracker.model.http.purchaseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUpiDetails {
    private String topHeader;
    private String topDescription;
    private String priceStag;
    private String backGroundColour;
    private String textColor;
    private String upiId;
    private String moneyInInr;
    private String moneyInUsd;
    private Long id;
    private String buttonColor;

    private String buttonBackGround;

    private String offerPrice;
    private String offerPercentage;
}
