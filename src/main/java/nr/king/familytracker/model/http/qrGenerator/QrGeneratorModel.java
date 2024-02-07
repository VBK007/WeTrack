package nr.king.familytracker.model.http.qrGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.Number;
import nr.king.familytracker.model.http.homeModel.HomeModel;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QrGeneratorModel implements Serializable {
    private HomeModel homeModel;
    private Number number;
}
