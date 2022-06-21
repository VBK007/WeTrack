package nr.king.familytracker.controller;

import nr.king.familytracker.model.http.GetPageHistoryNumberModel;
import nr.king.familytracker.repo.GetHistoryRepo;
import nr.king.familytracker.service.GetHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetPhoneHistoryModel extends BaseController{
    @Autowired
    private GetHistoryService getHistoryRepo;

    @PostMapping(value = "/v{version:[1]}/getUsersHistory")
    public ResponseEntity getUserHistory (@RequestBody GetPageHistoryNumberModel getPhoneHistoryModel)
    {
        return  getHistoryRepo.getAllPhonesHistory(getPhoneHistoryModel);
    }

}
