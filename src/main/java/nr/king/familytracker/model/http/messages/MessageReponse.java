package nr.king.familytracker.model.http.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageReponse {
    private ArrayList<MessageReponseBody> messageReponseBodyArrayList;
}
