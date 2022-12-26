package nr.king.familytracker.model.http.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponseBody {
    private ArrayList<AdminMessages> messagesArrayList;
}
