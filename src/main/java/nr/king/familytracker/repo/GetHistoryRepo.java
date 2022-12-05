package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.*;
import nr.king.familytracker.model.http.homeModel.CurrentPurchaseModel;
import nr.king.familytracker.model.http.homeModel.GetPhoneHistoryMainArrayModel;
import nr.king.familytracker.model.http.homeModel.GetPhoneNumberHistoryModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import static nr.king.familytracker.constant.LocationTrackingConstants.GET_LAST_HISTORY;
import static nr.king.familytracker.constant.LocationTrackingConstants.LOCAL_HOST_NUMBER;
import static nr.king.familytracker.constant.QueryConstants.SELECT_USER_EXPIRY_TIME;
import static nr.king.familytracker.constant.QueryConstants.UPDATE_PUSH_NOTIFICATION;

@Repository
public class GetHistoryRepo {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;
    private static final Logger logger = LogManager.getLogger(GetHistoryRepo.class);

    public ResponseEntity getAllPhonesHistory(GetPageHistoryNumberModel getPhoneHistoryModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME, getPhoneHistoryModel.getHomeModel().getId(),
                            getPhoneHistoryModel.getHomeModel().getPackageName());
            if (sqlRowSet.next()) {
                if (
                        commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                                getPhoneHistoryModel.getHomeModel().getPackageName(),
                                sqlRowSet.getInt("credit_limit")
                        )
                ) {
                    SendStatusListToMobileModel sendStatusListToMobileModels = new SendStatusListToMobileModel();
                    ArrayList<SendHistorystatusToAppModel> finalList = new ArrayList<>();
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN," +
                                            "CREATED_AT,UPDATED_AT,is_noti_enabled from NUMBER_FOR_USERS " +
                                            "where USER_ID=? and PACKAGE_NAME=? order by CREATED_AT desc",
                                    getPhoneHistoryModel.getHomeModel().getId(), getPhoneHistoryModel.getHomeModel().getPackageName());

                    while (numberSet.next()) {
                        getPhoneHistoryModel.setPhoneNumber(numberSet.getString("NUMBER"));
                        HttpResponse httpResponse = httpUtils.doPostRequest(0, GET_LAST_HISTORY,
                                commonUtils.getHeadersMap(numberSet.getString("TOKEN_HEADER")),
                                "Getting Phone Histories",
                                commonUtils.writeAsString(objectMapper, getPhoneHistoryModel));
                        GetPhoneHistoryMainArrayModel getPageHistoryNumberModel = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(), GetPhoneHistoryMainArrayModel.class);
                        SendHistorystatusToAppModel sendHistorystatusToAppModel = new SendHistorystatusToAppModel();
                        if (getPageHistoryNumberModel.getData().isEmpty()) {
                            sendHistorystatusToAppModel.setStatus(true);
                            sendHistorystatusToAppModel.setMessage("The Phone number status unavailable");
                            ArrayList<GetPhoneNumberHistoryModel> localList = new ArrayList<>();
                            GetPhoneNumberHistoryModel localModel = new GetPhoneNumberHistoryModel();
                            localModel.setNickName(numberSet.getString("NICK_NAME"));
                            localModel.setStatus("unavailable");
                            localModel.setPhoneNumber(numberSet.getString("NUMBER"));
                            localModel.setNotifyEnabled(numberSet.getBoolean("is_noti_enabled"));
                            localModel.setTimeStamp(getTimeStamp(numberSet.getString("CREATED_AT")) + "Z");
                            localList.add(localModel);
                            sendHistorystatusToAppModel.setStatusList(localList);
                        } else {
                            sendHistorystatusToAppModel.setStatus(true);
                            sendHistorystatusToAppModel.setMessage("The Phone number status available");
                            ArrayList<GetPhoneNumberHistoryModel> localList = new ArrayList<>();
                            for (int i = 0; i < getPageHistoryNumberModel.getData().size(); i++) {
                                if (i < 5) {
                                    GetPhoneNumberHistoryModel localModel = getPageHistoryNumberModel.getData().get(i);
                                    localModel.setNotifyEnabled(numberSet.getBoolean("is_noti_enabled"));
                                    localModel.setNickName(numberSet.getString("NICK_NAME"));
                                    localList.add(localModel);
                                } else {
                                    break;
                                }

                            }
                            sendHistorystatusToAppModel.setStatusList(localList);
                        }
                        finalList.add(sendHistorystatusToAppModel);
                    }

                    sendStatusListToMobileModels.setCurrentPurchaseModel(
                            new CurrentPurchaseModel(
                                    sqlRowSet.getString("Expiry_TIME"),
                                    sqlRowSet.getString("purchase_mode"),
                                    sqlRowSet.getInt("MAX_NUMBER")
                            )
                    );
                    sendStatusListToMobileModels.setSendHistorystatusToAppModelArrayList(finalList);
                    return responseUtils.constructResponse(200,
                            commonUtils.writeAsString(objectMapper, new ApiResponse(true, "Got All PhoneHistory", sendStatusListToMobileModels)));

                }
                else {
                    return responseUtils.constructResponse(200,
                            commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "Plan is Expired"
                                    )));
                }
            }
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "No User found to Get History"
                            )));
        } catch (Exception exception) {
            throw new FailedResponseException(exception.getMessage());
        }

    }

    private String getTimeStamp(String created_at) {
        String[] arraylist = created_at.split("");
        return arraylist[0] + "T" + arraylist[1];
    }

    public ResponseEntity enableNotification(NotificationModel notificationModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME, notificationModel.getUserId(),notificationModel.getPackageName());
            if (sqlRowSet.next()) {
                if (commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                        notificationModel.packageName,
                        sqlRowSet.getInt("CREDIT_LIMIT"))) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet(
                                    "select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS " +
                                            "where USER_ID=? and NUMBER=? and PACKAGE_NAME=?",
                                    notificationModel.getUserId(), String.valueOf(notificationModel.getNumberId()),notificationModel.getPackageName());

                    if (numberSet.next()) {
                        int count = updateNumberValue(notificationModel);
                        if (count == 1) {
                            notificationModel.setPushToken(numberSet.getString("PUSH_TOKEN"));
                            notificationModel.setHeaderToken(numberSet.getString("TOKEN_HEADER"));
                            notificationModel.setNickName(numberSet.getString("NICK_NAME"));
                            HttpResponse httpResponse = httpUtils.doPostRequest(0,
                                    LOCAL_HOST_NUMBER,
                                    commonUtils.getHeadersMap(numberSet.getString("TOKEN_HEADER")),
                                    "",
                                    commonUtils.writeAsString(objectMapper, notificationModel)
                            );

                            logger.info("httpResponse " + commonUtils.writeAsString(objectMapper, httpResponse.getResponse()));
                        }

                        return responseUtils.constructResponse(200,
                                commonUtils.writeAsString(objectMapper,
                                        new ApiResponse(
                                                (count == 1) ? true : false,
                                                (count == 1) ? "Push Notification Enabled" : "Unable to do Push Notification"
                                        )
                                )
                        );

                    }


                } else {
                    return responseUtils.constructResponse(200,
                            commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "Plan is Expired"
                                    )));
                }

            }

            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "No User found!"
                            )));

        } catch (Exception exception) {
            logger.error("Exception in enabling Notification" + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    public int updateNumberValue(NotificationModel notificationModel) {

        return jdbcTemplateProvider.getTemplate().update(UPDATE_PUSH_NOTIFICATION,
                notificationModel.isEnable(), notificationModel.getUserId(), notificationModel.getNumberId().toString(),
                notificationModel.getPackageName());

    }
}
