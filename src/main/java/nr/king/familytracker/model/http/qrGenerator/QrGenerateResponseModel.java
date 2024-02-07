package nr.king.familytracker.model.http.qrGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QrGenerateResponseModel implements Serializable {
    private List<QrServerResponse> qrServerResponseList;
}
