package nr.king.familytracker.model.http.youtubestreammodel.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BestVideoLong {
    private String videoUrl;
    private String createdDate;
    private String viewsCount;
    private String category;
    private String keywords;
    private String country;
    private String postId;
    private String leaqueStage;
}
