package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.service.ContactUsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactUsController extends  BaseController {


    @Autowired
    private ContactUsService contactUsService;

    @PostMapping("/v{version:[1]}/user/getfilterUser")
    public ResponseEntity getFilterData(@RequestBody HomeModel getPhoneHistoryModel)
    {
        return contactUsService.getContactUser(getPhoneHistoryModel);
    }
}
