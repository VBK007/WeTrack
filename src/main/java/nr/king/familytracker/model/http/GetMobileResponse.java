package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetMobileResponse {
    private String userId;
    private String mobileNumber;
    private String nickName;
    private  String countryCode;
    private String pushToken;
    private String expiryTime;
    private String expiryToken;
}
