package nr.king.familytracker.model.http.dashboardModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardResponses {
    private AccountModel accountModel;
    private FlashSales flashSales;
}
