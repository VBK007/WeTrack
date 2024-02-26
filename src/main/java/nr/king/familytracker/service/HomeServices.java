package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.PhoneModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.qrGenerator.QrGeneratorModel;
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
            if (commonUtils.checkHomeModelSecurityCheck(homeModel)) {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Invalid Characters Not allowed")));
            }
            return homeRepo.saveUserDetails(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in Storing the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to see User Details")));
        }
    }

    public ResponseEntity getAllMobileNumbers(PhoneModel phoneModel) {
        try {
            if (commonUtils.checkPhoneModelSecurityCheck(phoneModel)) {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Invalid Characters Not allowed")));
            }
            return homeRepo.getAllMobileNumbers(phoneModel);
        } catch (Exception exception) {
            logger.error("Exception in Storing the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to get Mobile Numbers")));
        }
    }


    public ResponseEntity getApiUrl(QrGeneratorModel homeModel) {
        try {
            if (commonUtils.checkQrGeneatorSecurityCheck(homeModel))
            {
                return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false,"Invalid characters Not allowed")));
            }

            return homeRepo.getApiUrl(homeModel);

        } catch (Exception exception) {
            logger.error("Exception in getApiUrl is "+exception.getMessage(),exception);
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to get Api Url")));
        }
    }


    public ResponseEntity verify_user(HomeModel homeModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(homeModel)) {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Invalid Characters Not allowed")));
            }
            return homeRepo.verify_user(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in verify  the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to verify User")));
        }
    }


    public ResponseEntity getUserNeed(HomeModel homeModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(homeModel)) {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Invalid Characters Not allowed")));
            }
            return homeRepo.getUserNeed(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in verify  the User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to verify User")));
        }
    }

    public ResponseEntity verifyAddUser(HomeModel homeModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(homeModel)) {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Invalid Characters Not allowed")));
            }
            return homeRepo.verifyAddUser(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in verify  the Add  User due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to verify User Add")));
        }
    }

    public ResponseEntity addMobileNumber(PhoneModel phoneModel) {
        try {
            if (commonUtils.checkPhoneModelSecurityCheck(phoneModel)) {
                return responseUtils.constructResponse(406,
                        commonUtils.writeAsString(objectMapper,
                                "Invalid Characters Not allowed "));
            }
            return homeRepo.addMobileNumber(phoneModel);
        } catch (Exception exception) {
            logger.error("Exception in addMobile Number   due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to add Number")));
        }
    }


    public ResponseEntity helloFromService() {
        try {
            return homeRepo.helloFromRepo();
        } catch (Exception exception) {
            logger.error("Error in hello world" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Error in api")));
        }
    }
}
