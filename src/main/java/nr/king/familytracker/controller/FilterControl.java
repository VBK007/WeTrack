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

    @PostMapping(value = "/v{version:[1]}/user/getfilterUser",produces = { "application/json" })
    public ResponseEntity getFilterData(@RequestBody FilterHistoryModel getPhoneHistoryModel)
    {
        return filterService.getFilterForUser(getPhoneHistoryModel);
    }

    @PostMapping(value = "/v{version:[1]}/user/getCompareUser",produces = { "application/json" })
    public ResponseEntity getCompareData(@RequestBody FilterHistoryModel getPhoneHistoryModel)
    {
        return filterService.getCompareData(getPhoneHistoryModel);
    }

}
