package nr.king.familytracker.model.http.adminSides;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashBoardResponses {
    private int totalNumberOfWeTrackUsers;
    private int totalNumbersAdded;
    private int totalNumberLoginByUsers;
    private int totalPurchased;
    private int totalPurchaseHistory;
    private int selectedCountryUser;
}
