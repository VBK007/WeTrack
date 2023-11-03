package nr.king.familytracker.model.http.youtubestreammodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.youtubestreammodel.Dashboard.BestVideoLong;
import nr.king.familytracker.model.http.youtubestreammodel.Dashboard.SliderBanner;
import nr.king.familytracker.model.http.youtubestreammodel.Dashboard.WallpaperModel;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeDashboardResponse {
    private HomeModel homeModel;
    private SliderBanner sliderBanner;
    private List<BestVideoLong> bannerShortsList;
    private List<BestVideoLong> bestVideoLongList;
    private boolean isGoogleSpace;
    private List<BestVideoLong> topTenMostViewedVideosList;
    private List<BestVideoLong> popularSuperOverMatch;
    private List<BestVideoLong> bestIplMatchesList;
    private List<BestVideoLong> greatCenturyList;
    private List<BestVideoLong> greatBowlingList;
    private List<WallpaperModel> wallpaperModelList;
}
