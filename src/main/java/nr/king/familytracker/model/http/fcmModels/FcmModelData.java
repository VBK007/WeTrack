package nr.king.familytracker.model.http.fcmModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FcmModelData implements Serializable {
  private String to;
  private Notification notification;
  private PushData data;

}