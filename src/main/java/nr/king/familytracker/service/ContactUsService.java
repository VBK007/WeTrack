package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.repo.ContactUserRepo;
import nr.king.familytracker.repo.FilterRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ContactUsService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ContactUserRepo filterRepo;


    @Autowired
    private ResponseUtils responseUtils;

    public ResponseEntity getContactUser(HomeModel homeModel)
    {
        try {
            return filterRepo.getInstaPackageName(homeModel);
        }
        catch (Exception exception)
        {
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to Get Instagram Data")));
        }
    }
}
