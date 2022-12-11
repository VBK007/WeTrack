package nr.king.familytracker.model.http.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import org.apache.kafka.common.protocol.types.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestBody {
    private HomeModel homeModel;
   private MessageReponseBody messageReponseBody;

}
