package nr.king.familytracker.model.http.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.kafka.common.protocol.types.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminMessages {
    private String lastMessgae;
    private String lastMessageTiming;

    private String userImageUrl;
    private String userId;
}
