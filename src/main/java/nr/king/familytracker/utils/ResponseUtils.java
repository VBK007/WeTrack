package nr.king.familytracker.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtils {

    public ResponseEntity constructResponse(Integer responseCode, String responseBody) {
        return ResponseEntity.status(responseCode).body(responseBody);
    }
}
