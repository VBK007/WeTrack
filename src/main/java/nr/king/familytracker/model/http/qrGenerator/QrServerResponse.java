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
public class QrServerResponse implements Serializable {
    private String user_id;
    private String whatsapp_status;
    private String qr_code;
}
