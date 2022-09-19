package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.purchaseModel.PremiumModels;
import nr.king.familytracker.model.http.purchaseModel.PurchaseRequestModel;
import nr.king.familytracker.model.http.purchaseModel.PurchaseUpdateRequestModel;
import nr.king.familytracker.model.http.purchaseModel.UpdateUpiDetails;
import nr.king.familytracker.repo.PurchaseRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class PurchaseService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;

    private static final Logger logger = LogManager.getLogger(PurchaseService.class);

    @Autowired
    private PurchaseRepo purchaseRepo;

    public ResponseEntity makeOrder(PurchaseRequestModel purchaseRequestModel) {
        try {
            if (commonUtils.checkPurchaseRequestModelSecurity(purchaseRequestModel))
            {
                return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false,"Invalid Characters")));
            }
            return purchaseRepo.makeOrder(purchaseRequestModel);
        } catch (Exception exception) {
            logger.error("Exception in purchase service due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to purchase ")));
        }
    }


    public ResponseEntity getUserAPI(HomeModel homeModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(homeModel))
            {
                return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false,"Invalid Characters")));
            }
                return  purchaseRepo.getUserAPI(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in GetUserAPI service due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,new ApiResponse(
                    false,
                    "Unable to get UPI values"
            )));
        }

    }

    public ResponseEntity updateTimingForTesting(HomeModel homeModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(homeModel))
            {
                return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false,"Invalid Characters")));
            }
            return  purchaseRepo.updateTiming(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in UpdateTimning service due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,new ApiResponse(
                    false,
                    "Unable to Update Time for Testing"
            )));
        }
    }

    public ResponseEntity inAppPurchase(PurchaseUpdateRequestModel purchaseUpdateRequestModel) {
        try {

            for (int i=0;i<purchaseUpdateRequestModel.getListofUpis().size();i++)
            {
                if (commonUtils.checkPremiumModel(purchaseUpdateRequestModel.getListofUpis().get(i)))
                {
                    return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false,"Invalid Characters")));
                }

            }

            return  purchaseRepo.updateAppPurchase(purchaseUpdateRequestModel);
        } catch (Exception exception) {
            logger.error("Exception in UpdateTimning service due to" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,new ApiResponse(
                    false,
                    "Unable to Update Time for Testing"
            )));
        }
    }
}
