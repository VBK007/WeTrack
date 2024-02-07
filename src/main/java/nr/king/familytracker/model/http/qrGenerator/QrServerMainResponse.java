package nr.king.familytracker.model.http.qrGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QrServerMainResponse implements Serializable {
    private QrServerResponse data;
}
