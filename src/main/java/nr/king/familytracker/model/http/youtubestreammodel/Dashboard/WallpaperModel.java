package nr.king.familytracker.model.http.youtubestreammodel.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WallpaperModel {
    private String imageUrl;
    private String postId;
    private String categoryId;
    private String imageName;
    private String createdAt;
    private String viewsCount;
    private String publishedId;
    private boolean isSale;
}
