package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.HttpResponse;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import static nr.king.familytracker.constant.LocationTrackingConstants.CREATE_USER;

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

    @Autowired
    private HttpUtils httpUtils;

    @Transactional
    public ResponseEntity saveUserDetails(HomeModel homeModel) {
        try {
            int count = doUpdateUser(homeModel);
            if (count == 0) {
                count = createUser(homeModel);
            }
            String authToken = "";

            if (count == 1) {
                authToken = UUID.randomUUID().toString();
            }
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(count == 1, "The api response is", authToken)));

        } catch (Exception exception) {
            logger.error("Exception in saveuser Details" + exception.getMessage(),
                    exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    @Transactional
    public ResponseEntity verify_user(HomeModel homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet("select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE from WE_TRACK_USERS where USER_ID=?", homeModel.getAppId());
            if (sqlRowSet.next()) {

                if (sqlRowSet.getBoolean("IS_USER_CREATED_IN_WETRACK_SERVICE")) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE from WE_TRACK_USERS where USER_ID=?",
                                    homeModel.getAppId());

                    if (Long.valueOf(String.valueOf(LocalDateTime.now())) >= Long.valueOf(sqlRowSet.getString("Expiry_TIME"))) {

                        while (numberSet.next()) {

                        }

                    } else {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Plan is Expired")));
                    }

                } else {
                    HttpResponse httpResponse = checkUserFromWeTrackService(homeModel);
                    if (httpResponse.getResponseCode() == 200) {
                        int count = doUpdateUserCreationinWetrack(homeModel);
                        if (count == 1) {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new
                                    ApiResponse(true,
                                    "User Created Successfully")));
                        }
                    }
                    return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new
                            ApiResponse(false,
                            "User Created UnSuccessfully")));

                }

            }

        } catch (Exception exception) {
            logger.error("Exception in  verify user due to is" + exception.getMessage(), exception);
            return responseUtils.constructResponse(406, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "Unable to verify User")));
        }
    }

    private int doUpdateUserCreationinWetrack(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set IS_USER_CREATED_IN_WETRACK_SERVICE=?,TOKEN_HEADER=? where USER_ID=?" +
                        true, homeModel.getAppId(), homeModel.getAppId());
    }

    private HttpResponse checkUserFromWeTrackService(HomeModel homeModel) throws IOException {
        return httpUtils.doPostRequest(0, CREATE_USER, new HashMap<>(), "Create User",
                commonUtils.writeAsString(objectMapper,
                        new HomeModel(
                                "Galaxy M23",
                                "g22ra2uijngsk7h6",
                                "g22ra2uijngsk7h6",
                                "Samsung",
                                "g22ra2uijngsk7h6",
                                "en_IN",
                                "g22ra2uijngsk7h6",
                                "3.1.3",
                                "19.2.1987"
                        )));
    }


    private int createUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate().update("insert into WE_TRACK_USERS " +
                        "(USER_ID,MOBILE_MODEL,IP_ADDRESS,COUNTRY,ONE_SIGNAL_EXTERNAL_USERID,MOBILE_VERSION,Expiry_TIME,IS_PURCHASED," +
                        "CREATED_AT,UPDATED_AT,IS_USER_CREATED_IN_WETRACK_SERVICE,TOKEN_HEADER,IS_NUMBER_ADDER) " +
                        "values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp,?,?,?)",
                homeModel.getAppId(), homeModel.getPhoneModel(), homeModel.getIpAddress(), homeModel.getCountryName(),
                homeModel.getOneSignalExternalUserId(), homeModel.getMobilePhone(), LocalDateTime.now().plusHours(3).toString(), false, false,
                "",false
        );
    }

    private int doUpdateUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set USER_ID=?,MOBILE_MODEL=?,IP_ADDRESS=?,COUNTRY=?," +
                                "ONE_SIGNAL_EXTERNAL_USERID=?,MOBILE_VERSION=?,UPDATED_AT = current_timestamp" +
                                "where USER_ID=?", homeModel.getAppId(), homeModel.getPhoneModel(), homeModel.getIpAddress(),
                        homeModel.getCountryName(), homeModel.getOneSignalExternalUserId(), homeModel.getMobilePhone(),
                        homeModel.getAppId()
                );
    }


}
