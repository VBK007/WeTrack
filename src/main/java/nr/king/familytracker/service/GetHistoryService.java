package nr.king.familytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.GetPageHistoryNumberModel;
import nr.king.familytracker.repo.GetHistoryRepo;
import nr.king.familytracker.repo.NotificationModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class GetHistoryService {
    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;

    @Autowired
    private GetHistoryRepo historyRepo;
    private static final Logger logger = LogManager.getLogger(GetHistoryService.class);
    public ResponseEntity getAllPhonesHistory(GetPageHistoryNumberModel getPhoneHistoryModel) {
        try {
            if (commonUtils.checkHomeModelSecurityCheck(getPhoneHistoryModel.getHomeModel())
            ||commonUtils.validate(Arrays.asList(commonUtils.isNullOrEmty(getPhoneHistoryModel.getPhoneNumber()))))
            {
                return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                        "Inavlid character Not Allowed"));
            }
            return  historyRepo.getAllPhonesHistory(getPhoneHistoryModel);
        }
        catch (Exception exception)
        {
            logger.error("Exception in getting all mobile Numbers "+exception.getMessage(),exception);
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to Get All User History")));
        }

    }

    public ResponseEntity enableNotification(NotificationModel notificationModel) {
        try
        {
            if (commonUtils.checkNotificationModelSecurity(notificationModel))
            {
                return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                        "Inavlid character Not Allowed"));
            }
            return historyRepo.enableNotification(notificationModel);
        }
        catch (Exception exception)
        {
            logger.error("Exception in enabling Notification"+exception.getMessage(),exception);
            return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false,"Unable to make Push Notification")));
        }

    }
}
