package nr.king.familytracker.scheduler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.repo.NotificationModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UpdateNotificationServices {

    @Autowired
    private ResponseUtils responseUtils;


    @Autowired
    private CommonUtils commonUtils;


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private UpdateNotificationRepo updateNotificationRepo;


    private  static  final Logger logger  = LogManager.getLogger(UpdateNotificationServices.class);


    public ResponseEntity updateNotifcationService(NotificationModel notificationModel)
    {
        try
        {
            return updateNotificationRepo.updateNotfication(notificationModel);

        }
        catch (Exception exception)
        {
            logger.error("Exception in update notification"+exception.getMessage(),exception);
            return  responseUtils.constructResponse(406,
                    commonUtils.writeAsString(objectMapper,new ApiResponse(false,"Unable to update notify")));
        }
    }

}
