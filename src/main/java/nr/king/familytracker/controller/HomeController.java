package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.service.HomeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class HomeController extends  BaseController{

    @Autowired
    private HomeServices homeServices;

    @PostMapping("/v{version:[1]}/create-deviceUser")
    public ResponseEntity storeUserData(@RequestBody HomeModel homeModel)
    {
        return homeServices.storeUsers(homeModel);
    }

 @PostMapping("/v{version:[1]}/user/getUserAbuzer")
    public ResponseEntity verifyUser(@RequestBody HomeModel homeModel)
    {
        return homeServices.verify_user(homeModel);
    }





}
