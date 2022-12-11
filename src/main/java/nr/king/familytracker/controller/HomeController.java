package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.PhoneModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.service.HomeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController extends BaseController {

    @Autowired
    private HomeServices homeServices;

    @PostMapping(value = "/v{version:[1]}/create-deviceUser",produces = { "application/json" })
    public ResponseEntity storeUserData(@RequestBody HomeModel homeModel) {
        return homeServices.storeUsers(homeModel);
    }

    @PostMapping(value = "/v{version:[1]}/user/getUserAbuzer",produces = { "application/json" })
    public ResponseEntity verifyUser(@RequestBody HomeModel homeModel) {
        return homeServices.verify_user(homeModel);
    }


    @PostMapping(value = "/v{version:[1]}/user/getUserneed",produces = { "application/json" })
    public ResponseEntity checkUserNumber(@RequestBody HomeModel homeModel) {
        return homeServices.getUserNeed(homeModel);
    }



    @PostMapping(value = "/v{version:[1]}/user/getUserAbuzerForAdd",produces = { "application/json" })
    public ResponseEntity verifyAddUser(@RequestBody HomeModel homeModel) {
        return homeServices.verifyAddUser(homeModel);
    }



    @PostMapping(value = "/v{version:[1]}/user/addNumberForUser",produces = { "application/json" })
    public ResponseEntity addMobileNumber(@RequestBody PhoneModel phoneModel) {
        return homeServices.addMobileNumber(phoneModel);
    }




    @PostMapping(value = "/v{version:[1]}/user/getAllMobileNumbers",produces = { "application/json" })
    public ResponseEntity getAllMobileNumbers(@RequestBody PhoneModel phoneModel) {
        return homeServices.getAllMobileNumbers(phoneModel);
    }


}
