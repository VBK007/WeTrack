package nr.king.familytracker.model.http.purchaseModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseUpdateRequestModel {
    private ArrayList<UpdateUpiDetails> listofUpis;
}
