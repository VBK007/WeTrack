package nr.king.familytracker.model.http.adminSides;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponseModel {
    private ArrayList<AdminDashBoardResponses> arrayList;
}
