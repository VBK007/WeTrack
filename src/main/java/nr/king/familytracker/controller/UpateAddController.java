package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.service.UpdateAddControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpateAddController extends BaseController{

@Autowired
private UpdateAddControllerService updateAddControllerService;

    @PostMapping("/v{version:[1]}/update-addCredits")
    public ResponseEntity storeUserData(@RequestBody HomeModel homeModel)
    {
        return updateAddControllerService.storeUsers(homeModel);
    }

}
