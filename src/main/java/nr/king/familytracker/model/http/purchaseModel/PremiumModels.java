package nr.king.familytracker.model.http.purchaseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PremiumModels {
    private String topHeader;
    private String topDescription;
    private String priceStag;
    private String moneyForOneDay;
    private String moneyForOneWeek;
    private String moneyForOneMonth;
    private String moneyForThreeMonth;
    private String moneyForOneYear;
    private String backGroundColour;
    private String textColor;
    private String productIds;
    private String symbolNative;
    private String moneyForThatCountry;
}
