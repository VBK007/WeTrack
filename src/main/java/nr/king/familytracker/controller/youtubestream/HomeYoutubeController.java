package nr.king.familytracker.controller.youtubestream;

import nr.king.familytracker.controller.BaseController;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.service.youtubeservice.YoutubeDashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeYoutubeController extends BaseController {

   @Autowired
   private YoutubeDashBoardService youtubeDashBoardService;

    @PostMapping(value = "/youtube/get-dashboard")
    public ResponseEntity getDashBoradForYoutube(@RequestBody  HomeModel homeModel)
    {
        return  youtubeDashBoardService.getYoutubeDashboard(homeModel);
    }


    @PostMapping(value = "/youtube/materialsync")
    public ResponseEntity syncMaterial(@RequestBody  HomeModel homeModel)
    {
        return  youtubeDashBoardService.syncMaterial(homeModel);
    }


    @PostMapping(value = "/youtube/createuser")
    public ResponseEntity createUser(@RequestBody HomeModel homeModel)
    {
        return youtubeDashBoardService.createUser(homeModel);
    }





}
