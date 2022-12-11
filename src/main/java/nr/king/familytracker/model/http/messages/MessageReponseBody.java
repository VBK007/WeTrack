package nr.king.familytracker.model.http.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageReponseBody {
    private String message;
    private String messageUserId;
    private String messageImageUrl;
    private String adminId;
    private boolean isSeen;

    private String messagerId;

    private String createdAt;
    private String updatedAt;

    private Long id;

    private int itemtype;
}
