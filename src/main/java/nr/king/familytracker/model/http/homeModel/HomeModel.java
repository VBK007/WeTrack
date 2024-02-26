package nr.king.familytracker.model.http.homeModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HomeModel implements Serializable {
  private String phoneModel;
  private String mobilePhone;
  private String oneSignalExternalUserId;
  private String phoneBrand;
  private String appId;
  private String countryName;
  private String id;
  private String version;
  private String ipAddress;
  private String packageName;
  private boolean adding;
}
