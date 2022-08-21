package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.HttpResponse;
import nr.king.familytracker.model.http.PhoneModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;
import static nr.king.familytracker.constant.QueryConstants.*;

@Repository
public class UpdateNotificationRepo {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplateProvider jdbcTemplate;

    @Autowired
    private HttpUtils httpUtils;

private static final Logger logger  = LogManager.getLogger(UpdateNotificationRepo.class);

    public void doPushNotifcation() {
        try {

            SqlRowSet sqlRowSet = jdbcTemplate.getTemplate().queryForRowSet(GET_NOT_DEMO_USERS);
            while (sqlRowSet.next()) {
                SqlRowSet numberset = jdbcTemplate.getTemplate().queryForRowSet(selectNumberWithToken,sqlRowSet.getString("user_id"));
                if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME") )
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                ) {
                    while (numberset.next())
                    {
                        PhoneModel phoneModel = new PhoneModel();
                        phoneModel.setId(sqlRowSet.getString("user_id"));
                        phoneModel.setPhoneNumber(numberset.getString("number"));
                        phoneModel.setCountryCode(numberset.getString("COUNTRY_CODE"));
                        addNewNumbeIntoWeTrackServer(phoneModel,false);
                        Thread.sleep(1000,500);
                    }
                }
            }

        } catch (Exception exception) {
            logger.error("Exception in schedualr " + exception.getMessage(), exception);
        }
    }


    private void addNewNumbeIntoWeTrackServer(PhoneModel phoneModel, boolean isFirstTime) throws IOException {
        HomeModel homeModel = commonUtils.getHomeModel(phoneModel.getId(), isFirstTime);

            HttpResponse httpResponse = httpUtils.doPostRequest(
                    0,
                    CREATE_USER,
                    commonUtils.getHeadersMap(homeModel.getId()),
                    "Create New User for Mobile Creation",
                    commonUtils.writeAsString(objectMapper,
                            homeModel)
            );
            logger.info("Response in adding number with schedulers " + httpResponse.getResponse() + "\n" + httpResponse.getResponse());

            if (httpResponse.getResponseCode() == 200) {
                HttpResponse innerMobileRequest = httpUtils.doPostRequest(0,
                        POST_NUMBER,
                        commonUtils.getHeadersMap(homeModel.getId()),
                        "Adding Number for  Mobile Creation",
                        commonUtils.writeAsString(objectMapper,
                                phoneModel
                        ));
                if (innerMobileRequest.getResponseCode() == 200) {
                   logger.info("Response in added number with new user"+innerMobileRequest.getResponseCode());
                   updatePhoneNumberDetails(phoneModel,homeModel);
                }
            }
        }

    private void doNotifyInNewServer(NotificationModel notificationModel, String id)  {
        try {
            HttpResponse enableSchedularPush =
                    httpUtils.doPostRequest(0,
                            LOCAL_HOST_NUMBER,
                            commonUtils.getHeadersMap(id),
                            "",
                            commonUtils.writeAsString(objectMapper,notificationModel)
                    );
            logger.info("enablePush Notification"+enableSchedularPush.getResponseCode());
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }





    private int updatePhoneNumberDetails(PhoneModel phoneModel, HomeModel homeModel) {
        return jdbcTemplate.getTemplate()
                .update(UPDATE_HEADER_FOR_MOBILE,
                        homeModel.getId(),
                        phoneModel.getId(),
                        phoneModel.getPhoneNumber());
    }





}
