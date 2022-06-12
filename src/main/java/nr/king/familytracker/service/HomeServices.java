package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.PhoneModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.repo.HomeRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class HomeServices {
    @Autowired
    private ResponseUtils responseUtils;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HomeRepo homeRepo;


    private static final Logger logger = LogManager.getLogger(HomeServices.class);


    public ResponseEntity storeUsers(HomeModel homeModel) {
        try {
            return homeRepo.saveUserDetails(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in Storing the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to see User Details")));
        }
    }
    public ResponseEntity getAllMobileNumbers(PhoneModel phoneModel) {
        try {
            return homeRepo.getAllMobileNumbers(phoneModel);
        } catch (Exception exception) {
            logger.error("Exception in Storing the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to get Mobile Numbers")));
        }
    }


    public ResponseEntity verify_user(HomeModel homeModel) {
        try {
            return homeRepo.verify_user(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in verify  the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to verify User")));
        }
    }

    public ResponseEntity addMobileNumber(PhoneModel phoneModel) {
        try {
            return homeRepo.addMobileNumber(phoneModel);
        } catch (Exception exception) {
            logger.error("Exception in addMobile Number  due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to add Number")));
        }
    }


}
