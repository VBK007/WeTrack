package nr.king.familytracker.model.http.homeModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.String;

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
}
