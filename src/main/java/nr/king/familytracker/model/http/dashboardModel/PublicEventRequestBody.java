package nr.king.familytracker.model.http.dashboardModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventRequestBody {
    private List<FlashSales> flashSalesList;

}
