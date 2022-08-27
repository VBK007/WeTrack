package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.repo.FilterRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FilterService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private FilterRepo filterRepo;


    @Autowired
    private ResponseUtils responseUtils;


    public ResponseEntity getFilterForUser(FilterHistoryModel filterHistoryModel)
    {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(filterHistoryModel.getHomeModel())||
                    commonUtils.checkFilterHistoryModel(filterHistoryModel))
            {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                "Invalid Characters Not allowed "));
            }
            return filterRepo.getFilterData(filterHistoryModel);
        }
        catch (Exception exception)
        {
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to Get Filter Data")));
        }
    }


    public ResponseEntity getCompareData(FilterHistoryModel getPhoneHistoryModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(getPhoneHistoryModel.getHomeModel())||
                    commonUtils.checkFilterHistoryModel(getPhoneHistoryModel))
            {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                "Invalid Characters Not allowed "));
            }
            return filterRepo.getCompareData(getPhoneHistoryModel);
        }
        catch (Exception exception)
        {
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to Get Filter Data")));
        }
    }
}
