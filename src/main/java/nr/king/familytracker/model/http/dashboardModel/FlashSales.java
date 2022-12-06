package nr.king.familytracker.model.http.dashboardModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FlashSales {
    private String  flashImageUrl;
    private String flashTitle;
    private String flashBody;
    private boolean navigateToPremium;

    private boolean showFlash;
    private String eventImageUrl;
    private String mornigImageUrl;

    private  String afternoonImageUrl;

    private String  eveningImageUrl;

    private String  nightImageUrl;

    private String eventId;
    private String countryName;

}
