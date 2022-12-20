package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.HttpResponse;
import nr.king.familytracker.model.http.dashboardModel.*;
import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.model.http.homeModel.GetPhoneHistoryMainArrayModel;
import nr.king.familytracker.model.http.homeModel.GetPhoneNumberHistoryModel;
import nr.king.familytracker.model.http.messages.MessageReponse;
import nr.king.familytracker.model.http.messages.MessageReponseBody;
import nr.king.familytracker.model.http.messages.MessageRequestBody;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;
import static nr.king.familytracker.constant.QueryConstants.*;

@Repository
public class DashBoardRepo {
    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private ResponseUtils responseUtils;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    @Autowired
    private HttpUtils httpUtils;
    private static final Logger logger = LogManager.getLogger(DashBoardRepo.class);

    public ResponseEntity getDashBoardResponse(DashBoardRequestBody homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME, homeModel.getHomeModel().getId(),
                            homeModel.getHomeModel().getPackageName());
            if (sqlRowSet.next()) {
                boolean isAccountExpired = commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                        homeModel.getHomeModel().getPackageName(),
                        sqlRowSet.getInt("credit_limit"));
                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                        getAccountModel(isAccountExpired, sqlRowSet, homeModel)));
            }
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "No User found to Get Dashboard"
                            )));
        } catch (Exception exception) {
            logger.error("Exception in the Dashboard Response is " + exception.getMessage(), exception);
            throw new FailedResponseException("Exception in Dashboard Responses");
        }
    }

    private DashBoardResponses getAccountModel(boolean isAccountExpired, SqlRowSet sqlRowSet, DashBoardRequestBody dashBoardRequestBody) {
        DashBoardResponses dashBoardResponses = new DashBoardResponses();
        AccountModel accountModel = new AccountModel();
        List<AccountNumberWithName> accountNumbersList = new ArrayList<>();
        accountModel.setCreatedAt(sqlRowSet.getString("CREATED_AT"));
        accountModel.setExpiryAt(sqlRowSet.getString("EXPIRY_TIME"));
        accountModel.setShowAdd((Objects.equals(sqlRowSet.getString("PURCHASE_MODE"), "demo")));
        accountModel.setPurchaseMode(sqlRowSet.getString("PURCHASE_MODE"));
        accountModel.setTracking(isAccountExpired);
        accountModel.setTrackingTime(commonUtils.checkTimeDifference(sqlRowSet.getString("CREATED_AT")));
        accountModel.setAppVersion(APP_VERSION);
        accountModel.setForceUpdate(IS_FORCE_UPDATE);
        accountModel.setAppInActive(IS_APP_INACTIVE);
        accountModel.setDemoMode(IS_MONEY_MODE);
        accountModel.setCountryBasedSubscription(commonUtils.checkCountryState(dashBoardRequestBody.getHomeModel().getCountryName()));
        SqlRowSet innerNumberSet = jdbcTemplateProvider.getTemplate()
                .queryForRowSet(selectNumberWithToken, dashBoardRequestBody.getHomeModel().getId(),
                        dashBoardRequestBody.getHomeModel().getPackageName());
        while (innerNumberSet.next()) {
            accountNumbersList.add(new AccountNumberWithName(innerNumberSet.getString("NUMBER"), innerNumberSet.getString("NICK_NAME")));
            //for getting social Media Activity
            if ((dashBoardRequestBody.getNumber().isEmpty() && innerNumberSet.isFirst()) ||
                    dashBoardRequestBody.getNumber().equals(innerNumberSet.getString("NUMBER"))) {
                accountModel.setAccountNumberSocialMediaActivity(getSocialMediaActivity(innerNumberSet, dashBoardRequestBody, sqlRowSet.getString("MAX_NUMBER")));
            }
        }
        if (accountModel.getAccountNumberSocialMediaActivity() == null) {
            accountModel.setAccountNumberSocialMediaActivity(new AccountNumberSocialMediaActivity(
                    0,
                    0,
                    0,
                    Integer.parseInt(commonUtils.isNullOrEmty(sqlRowSet.getString("MAX_NUMBER")))));
        }
        //appversion
        FlashSales flashSales = getFlashSales(dashBoardRequestBody, dashBoardResponses);
        dashBoardResponses.setFlashSales(flashSales);
        dashBoardResponses.setFlashSales(flashSales);
        accountModel.setAccountNumbers(new AccountNumbers(accountNumbersList));
        dashBoardResponses.setAccountModel(accountModel);
        return dashBoardResponses;
    }


    private AccountNumberSocialMediaActivity getSocialMediaActivity(SqlRowSet innerNumberSet, DashBoardRequestBody dashBoardRequestBody, String maxNumber) {
        AccountNumberSocialMediaActivity accountNumberSocialMediaActivity = new AccountNumberSocialMediaActivity();
        try {
            FilterHistoryModel localFilterModel = new FilterHistoryModel();
            localFilterModel.setStartDate(dashBoardRequestBody.getFromDate());
            localFilterModel.setEndDate(dashBoardRequestBody.getToDate());
            localFilterModel.setPageLimit(400);
            localFilterModel.setStart(0);
            localFilterModel.setPhoneNumber(innerNumberSet.getString("NUMBER"));
            HttpResponse httpResponse = httpUtils.doPostRequest(0,
                    GET_HISTORY,
                    commonUtils.getHeadersMapForSpecific(innerNumberSet.getString("TOKEN_HEADER")),
                    "Getting Filter Data",
                    commonUtils.writeAsString(objectMapper, localFilterModel)
            );
            GetPhoneHistoryMainArrayModel getPageHistoryNumberModel = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(),
                    GetPhoneHistoryMainArrayModel.class);
            DashBoardTimeSpending dashBoardTimeSpending = getDashBoardTiming(getPageHistoryNumberModel);
            accountNumberSocialMediaActivity.setTotalNumberOfHours(dashBoardTimeSpending.getTotalTimeSpent());
            accountNumberSocialMediaActivity.setTotalNumberOfOnline(dashBoardTimeSpending.getTotalTimeOnline());
            accountNumberSocialMediaActivity.setTotalNumberOfOffline(dashBoardTimeSpending.getTotalTimeOffline());
            accountNumberSocialMediaActivity.setMaxNumber(Integer.parseInt(maxNumber));
        } catch (Exception exception) {
            logger.error("Exception in api call for number of timeCalculation" + exception.getMessage(), exception);
        }
        return accountNumberSocialMediaActivity;
    }

    private DashBoardTimeSpending getDashBoardTiming(GetPhoneHistoryMainArrayModel getPageHistoryNumberModel) {
        int totalOnline = 0;
        int totalOffline = 0;
        int totalTimeSpend = 0;
        DashBoardTimeSpending dashBoardTimeSpending = new DashBoardTimeSpending();
        for (int i = 0; i < getPageHistoryNumberModel.getData().size(); i++) {
            GetPhoneNumberHistoryModel lData = getPageHistoryNumberModel.getData().get(i);
            if ("available".equalsIgnoreCase(lData.getStatus())) {
                totalOnline++;
            } else {
                totalOffline++;
            }//total time count
            //need to add second to minutes
            if (i % 2 != 0) {
                totalTimeSpend += commonUtils.getTimeDuration(getPageHistoryNumberModel.getData().get(i - 1).timeStamp, lData.timeStamp);
            } else if (i == getPageHistoryNumberModel.getData().size() - 1 && i != 0) {
                totalTimeSpend += commonUtils.getTimeDuration(getPageHistoryNumberModel.getData().get(i - 1).timeStamp, lData.timeStamp);
            }
        }
        dashBoardTimeSpending.setTotalTimeSpent(totalTimeSpend);
        dashBoardTimeSpending.setTotalTimeOnline(totalOnline);
        dashBoardTimeSpending.setTotalTimeOffline(totalOffline);
        return dashBoardTimeSpending;
    }

    private FlashSales getFlashSales(DashBoardRequestBody dashBoardRequestBody, DashBoardResponses dashBoardResponses) {
        FlashSales flashSales = new FlashSales();
        String countryName = commonUtils.checkCountryState(dashBoardRequestBody.getHomeModel().getCountryName());
        SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_EVENT_BASED_ON_COUNTRY,
                countryName);
        if (sqlRowSet.next()) {
            SqlRowSet showFlashSales = jdbcTemplateProvider.getTemplate().queryForRowSet(
                    SELECT_COUNTRY_VALUE, sqlRowSet.getString("EVENT_ID"),
                    dashBoardRequestBody.getHomeModel().getId());
            flashSales.setFlashBody(sqlRowSet.getString("EVENT_BODY"));
            flashSales.setFlashTitle(sqlRowSet.getString("EVENT_NAME"));
            flashSales.setEventImageUrl(sqlRowSet.getString("EVENT_IMAGE"));
            flashSales.setFlashImageUrl(sqlRowSet.getString("EVENT_NORMAL_IMAGE"));
            flashSales.setMornigImageUrl(sqlRowSet.getString("IMAGE_MORNING"));
            flashSales.setAfternoonImageUrl(sqlRowSet.getString("IMAGE_AFTERNOON"));
            flashSales.setEveningImageUrl(sqlRowSet.getString("IMAGE_EVENING"));
            flashSales.setNightImageUrl(sqlRowSet.getString("IMAGE_NIGHT"));
            flashSales.setEventId(sqlRowSet.getString("EVENT_ID"));
            flashSales.setShowFlash(!showFlashSales.next());
            flashSales.setNavigateToPremium(!sqlRowSet.next());
        }
        return flashSales;
    }

    public ResponseEntity publishPublicEvent(PublicEventRequestBody publicEventRequestBody) {
        try {
            int count = 0;
            for (int i = 0; i < publicEventRequestBody.getFlashSalesList().size(); i++) {
                count = updatePublicEvent(publicEventRequestBody.getFlashSalesList().get(i), i + 1);
                if (count == 0) {
                    count = createPublicEvent(publicEventRequestBody.getFlashSalesList().get(i));
                }
            }
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(count == 1, (count == 1) ? "Public Event Published" : "Unable to publish Public Event")));

        } catch (Exception exception) {
            throw new FailedResponseException("Exception is " + exception.getMessage());
        }
    }

    private int createPublicEvent(FlashSales flashSales) {
        return jdbcTemplateProvider.getTemplate().update(INSERT_VALUES_IN_EVENT,
                flashSales.getFlashTitle(), flashSales.getEventImageUrl(),
                flashSales.getFlashImageUrl(), flashSales.getFlashBody(), flashSales.getCountryName(), flashSales.getMornigImageUrl(), flashSales.getAfternoonImageUrl(),
                flashSales.getEveningImageUrl(), flashSales.getNightImageUrl(), flashSales.getEventId()
        );
    }

    private int updatePublicEvent(FlashSales flashSales, int id) {
        return jdbcTemplateProvider.getTemplate().update(UPDATE_VALUES_IN_EVENT, flashSales.getFlashTitle(), flashSales.getEventImageUrl(),
                flashSales.getFlashImageUrl(), flashSales.getFlashBody(), flashSales.getCountryName(), flashSales.getMornigImageUrl(), flashSales.getAfternoonImageUrl(),
                flashSales.getEveningImageUrl(), flashSales.getNightImageUrl(), flashSales.getEventId(), id);

    }


    public ResponseEntity postPublicEvent(FlashSales flashSales) {
        try {
            int count = updatePostUserEventClick(flashSales);
            if (count == 0) {
                count = createPostUserEventClickEvent(flashSales);
            }
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(count == 1, (count == 1) ? "Posted Successfully" : "Post Failed")));
        } catch (Exception exception) {
            throw new FailedResponseException("Exception in Post Public Event " + exception.getMessage());
        }
    }

    private int createPostUserEventClickEvent(FlashSales flashSales) {
        return jdbcTemplateProvider.getTemplate().update(INSERT_POST_CLICK_VALUES, flashSales.getEventId(), flashSales.getFlashTitle(), flashSales.getFlashBody());
    }

    private int updatePostUserEventClick(FlashSales flashSales) {
        return jdbcTemplateProvider.getTemplate().update(UPDATE_POST_CLICK_VALUES, flashSales.getEventId(), flashSales.getFlashTitle(),
                flashSales.getFlashBody(), flashSales.getEventId());
    }

    public ResponseEntity postUserMessage(MessageRequestBody flashSales) {
        try {
            if (!commonUtils.isNullOrEmtys(flashSales.getMessageReponseBody().getMessage()) || !commonUtils.isNullOrEmtys(flashSales.getMessageReponseBody().getMessageImageUrl())) {
                int count = updateMesageUser(flashSales);
                if (count == 0) {
                    count = insertMessgaeUser(flashSales);
                }
                logger.info("Message Inserted if Any Quries" + count);
            }

            ArrayList<MessageReponseBody> messageReponseBodyArrayList = (ArrayList<MessageReponseBody>) jdbcTemplateProvider.getTemplate().query(SELECT_ALL_MESSAGE, this::mapLocationHistoryRow);
            messageReponseBodyArrayList = getLastMessageCheck(messageReponseBodyArrayList);
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new MessageReponse(messageReponseBodyArrayList)
            ));
        } catch (Exception exception) {
            throw new FailedResponseException("Exception in Post User message");
        }
    }

    private ArrayList<MessageReponseBody> getLastMessageCheck(ArrayList<MessageReponseBody> messageReponseBody) {
        ArrayList<MessageReponseBody> lData = new ArrayList<>(messageReponseBody);
        MessageReponseBody innerData = new MessageReponseBody();
        innerData.setItemtype(1);
        innerData.setMessage((messageReponseBody.isEmpty())?EMPTY_STRING:WILL_UPDATE_STRING);
        innerData.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        innerData.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
        innerData.setAdminId(CURRECY_CONVERTER);
        innerData.setSeen(false);
        if (messageReponseBody.isEmpty() || !messageReponseBody.get(messageReponseBody.size()-1).getMessagerId().equals(CURRECY_CONVERTER))
        {
            innerData.setMessagerId(CURRECY_CONVERTER);
            innerData.setMessageUserId(CURRECY_CONVERTER);
            lData.add(innerData);
        }
        return lData;
    }

    private MessageReponseBody mapLocationHistoryRow(ResultSet resultSet, int i) throws SQLException {
        MessageReponseBody lData = new MessageReponseBody();
        lData.setId(resultSet.getLong("ID"));
        lData.setMessage(resultSet.getString("MESSAGE"));
        lData.setMessageImageUrl(resultSet.getString("MESSAGE_IMAGE_URL"));
        lData.setAdminId(resultSet.getString("ADMIN_ID"));
        lData.setSeen(resultSet.getBoolean("IS_SEEN"));
        lData.setMessagerId(resultSet.getString("MESSAGER_ID"));
        lData.setMessageUserId(resultSet.getString("MESSAGE_USER_ID"));
        lData.setCreatedAt(String.valueOf(commonUtils.getTimeValue(resultSet.getString("CREATED_AT"))));
        lData.setUpdatedAt(String.valueOf(commonUtils.getTimeValue(resultSet.getString("UPDATED_AT"))));
        lData.setItemtype((resultSet.getString("MESSAGER_ID").equals(resultSet.getString("MESSAGE_USER_ID"))) ? 0 : 1);
        return lData;
    }

    private int insertMessgaeUser(MessageRequestBody flashSales) {
        MessageReponseBody lData = flashSales.getMessageReponseBody();
        return jdbcTemplateProvider.getTemplate().update(INSERT_PUBLIC_MESSAGE_VALUES,
                lData.getMessage(), flashSales.getHomeModel().getId(),
                lData.getMessageImageUrl(), CURRECY_CONVERTER, false, flashSales.getHomeModel().getId()
        );
    }

    private int updateMesageUser(MessageRequestBody flashSales) {
        return jdbcTemplateProvider.getTemplate().update(UPDATE_PUBLIC_MESSAGE_VALUES,
                flashSales.getMessageReponseBody().getMessage(), flashSales.getMessageReponseBody().
                        getMessageImageUrl(), flashSales.getHomeModel().getId(), flashSales.getMessageReponseBody().getId());
    }
}
