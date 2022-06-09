package nr.king.familytracker.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ApiResponse {
    private boolean status;
    private String message;
    private String refId;
    private String sessionId;

    private Object data;

    public ApiResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiResponse(boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean status, String message, String sessionId) {
        this.status = status;
        this.message = message;
        this.sessionId = sessionId;
    }
}
