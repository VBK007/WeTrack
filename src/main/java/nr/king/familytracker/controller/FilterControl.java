package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FilterControl  extends BaseController{

    @Autowired
    private FilterService filterService;

    @PostMapping("/v{version:[1]}/user/getfilterUser")
    public ResponseEntity getFilterData(@RequestBody FilterHistoryModel getPhoneHistoryModel)
    {
        return filterService.getFilterForUser(getPhoneHistoryModel);
    }

    @PostMapping("/v{version:[1]}/user/getCompareUser")
    public ResponseEntity getCompareData(@RequestBody FilterHistoryModel getPhoneHistoryModel)
    {
        return filterService.getCompareData(getPhoneHistoryModel);
    }

}
