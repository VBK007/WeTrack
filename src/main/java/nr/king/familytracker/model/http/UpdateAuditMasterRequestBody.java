package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAuditMasterRequestBody {
    private HomeModel homeModel;
    private String modules;
    private String task;
    private String today;
}
