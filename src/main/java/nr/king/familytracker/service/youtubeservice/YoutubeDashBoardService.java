package nr.king.familytracker.service.youtubeservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.model.http.FailedResponseModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.repo.youtubeRepo.YouTubeDashboardServiceRepo;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class YoutubeDashBoardService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;


    @Autowired
    private YouTubeDashboardServiceRepo youtubeDashRepo;


    private static Logger logger = LogManager.getLogger(YoutubeDashBoardService.class);

    public ResponseEntity getYoutubeDashboard(HomeModel homeModel) {
        try {

        } catch (Exception exception) {
            logger.error("The exception in getYoutubeDashboard" + exception.getMessage(), exception);
        }
        return responseUtils.constructResponse(406,commonUtils.writeAsString(objectMapper,
                new FailedResponseModel("Unable to Proceed",406)));

    }


    public ResponseEntity syncMaterial(HomeModel homeModel) {
        try {

            return  youtubeDashRepo.syncMaterial(homeModel);
        } catch (Exception exception) {
            logger.error("The exception in getYoutubeDashboard" + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }



}
