package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class HomeRepo {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;

    private static final Logger logger = LogManager.getLogger(HomeRepo.class);


    @Transactional
    public ResponseEntity saveUserDetails(HomeModel homeModel)
    {
        try {
            int count = doUpdateUser(homeModel);
            if (count==0)
            {
                count = createUser(homeModel);
            }
            String authToken="";

            if (count == 1)
            {
                authToken = UUID.randomUUID().toString();
            }

            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                    new ApiResponse(count==1,"The api response is",authToken)));

        }
        catch (Exception exception)
        {
            logger.error("Exception in saveuser Details"+exception.getMessage(),
                    exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    private int createUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate().update("insert into WE_TRACK_USERS " +
                "(USER_ID,MOBILE_MODEL,IP_ADDRESS,COUNTRY,ONE_SIGNAL_EXTERNAL_USERID,MOBILE_VERSION,Expiry_TIME,IS_PURCHASED," +
                "CREATED_AT,UPDATED_AT) values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp)",
                homeModel.getAppId(),homeModel.getPhoneModel(),homeModel.getIpAddress(),homeModel.getCountryName(),
                homeModel.getOneSignalExternalUserId(),homeModel.getMobilePhone(), LocalDateTime.now().plusHours(3).toString(),false
                );
    }

    private int doUpdateUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set USER_ID=?,MOBILE_MODEL=?,IP_ADDRESS=?,COUNTRY=?," +
                        "ONE_SIGNAL_EXTERNAL_USERID=?,MOBILE_VERSION=?,UPDATED_AT = current_timestamp" +
                        "where USER_ID=?",homeModel.getAppId(),homeModel.getPhoneModel(),homeModel.getIpAddress(),
                        homeModel.getCountryName(),homeModel.getOneSignalExternalUserId(),homeModel.getMobilePhone(),
                        homeModel.getAppId()
                        );
    }


}
