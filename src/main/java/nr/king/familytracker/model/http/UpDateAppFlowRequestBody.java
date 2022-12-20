package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpDateAppFlowRequestBody {
    private String appVersion;
    private boolean isForceUpdate;
    private boolean isAppIsActive;
    private boolean isOneYearMode;
}
