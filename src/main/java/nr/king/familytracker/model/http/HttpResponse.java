package nr.king.familytracker.model.http;

import com.squareup.okhttp.Headers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HttpResponse {
    int responseCode;
    String response;
    Headers responseHeaders;

    public HttpResponse(int responseCode, String response) {
        this.responseCode = responseCode;
        this.response = response;
    }
}
