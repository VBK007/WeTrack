package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.dashboardModel.DashBoardRequestBody;
import nr.king.familytracker.model.http.dashboardModel.FlashSales;
import nr.king.familytracker.model.http.dashboardModel.PublicEventRequestBody;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.messages.MessageRequestBody;
import nr.king.familytracker.repo.DashBoardRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DashBoardService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private ResponseUtils responseUtils;


    @Autowired
    private DashBoardRepo dashBoardRepo;

    private static final Logger logger = LogManager.getLogger(DashBoardService.class);

    public ResponseEntity getDashBoardFragment(DashBoardRequestBody homeModel) {
        try {
            if (commonUtils.checkDashBoardRequestModel(homeModel)) {
                return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                        "Invalid Characters Not allowed")));
            }
            return dashBoardRepo.getDashBoardResponse(homeModel);
        } catch (Exception exception) {
            logger.error("Exception in the dashboard service " + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                    "Invalid Format Not allowed")));
        }
    }


    public ResponseEntity publishPublicEvent(PublicEventRequestBody publicEventRequestBody) {
        try
        {
           return dashBoardRepo.publishPublicEvent(publicEventRequestBody);
        }
        catch (Exception exception)
        {
            logger.error("Exception in the dashboard service " + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                    "Invalid Format Not allowed")));
        }
    }

    public ResponseEntity postPublicEventByUser(FlashSales flashSales) {
        try
        {
            if (commonUtils.checkFlashSalesSecurityCheck(flashSales))
            {
                return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                        "Invalid Format Not allowed")));
            }

            return dashBoardRepo.postPublicEvent(flashSales);
        }
        catch (Exception exception)
        {
            logger.error("Exception in the dashboard service " + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                    "Invalid Format Not allowed")));
        }
    }

    public ResponseEntity postMessageToUser(MessageRequestBody flashSales) {
        try
        {
            if (commonUtils.checkMessageRequestBodySecurityCheck(flashSales))
            {
                return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                        "Invalid Format Not allowed")));
            }

            return dashBoardRepo.postUserMessage(flashSales);
        }
        catch (Exception exception)
        {
            logger.error("Exception in the post  message " + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                    "Invalid Format Not allowed")));
        }
    }
}
