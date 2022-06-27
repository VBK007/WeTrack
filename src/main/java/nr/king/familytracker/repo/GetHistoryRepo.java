package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.controller.GetPhoneHistoryModel;
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
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static nr.king.familytracker.constant.LocationTrackingConstants.GET_LAST_HISTORY;
import static nr.king.familytracker.constant.QueryConstants.SELECT_USER_EXPIRY_TIME;

@Component
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
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME, getPhoneHistoryModel.getHomeModel().getId());
            if (sqlRowSet.next()) {
                if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME"))
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()) {
                    SendStatusListToMobileModel sendStatusListToMobileModels = new SendStatusListToMobileModel();
                    ArrayList<SendHistorystatusToAppModel> finalList = new ArrayList<>();

                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS " +
                                            "where USER_ID=?",
                                    getPhoneHistoryModel.getHomeModel().getId());
                    while (numberSet.next()) {
                        getPhoneHistoryModel.setPhoneNumber(numberSet.getString("NUMBER"));
                        HttpResponse httpResponse = httpUtils.doPostRequest(0, GET_LAST_HISTORY,
                                commonUtils.getHeadersMap(numberSet.getString("TOKEN_HEADER")),
                                "Getting Phone Histories",
                                commonUtils.writeAsString(objectMapper, getPhoneHistoryModel));
                        GetPhoneHistoryMainArrayModel getPageHistoryNumberModel = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(), GetPhoneHistoryMainArrayModel.class);
                        SendHistorystatusToAppModel sendHistorystatusToAppModel = new SendHistorystatusToAppModel();
                        if (getPageHistoryNumberModel.getData().isEmpty()) {
                            sendHistorystatusToAppModel.setStatus(false);
                            sendHistorystatusToAppModel.setMessage("The Phone number status unavailable");
                        } else {
                            sendHistorystatusToAppModel.setStatus(true);
                            sendHistorystatusToAppModel.setMessage("The Phone number status available");
                            ArrayList<GetPhoneNumberHistoryModel> localList = new ArrayList<>();
                            GetPhoneNumberHistoryModel localModel = getPageHistoryNumberModel.getData().get(0);
                            localModel.setNickName(numberSet.getString("NICK_NAME"));
                            localList.add(localModel);
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

                } else {
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
}
