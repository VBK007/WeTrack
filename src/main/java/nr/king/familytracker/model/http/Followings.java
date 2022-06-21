package nr.king.familytracker.model.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Followings implements Serializable {
  private Number number;
  private String nickName;
  private Boolean enablePush;
  private Integer numberId;
  private Boolean isActive;
  private Integer userId;

}