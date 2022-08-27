package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.*;
import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.model.http.homeModel.GetPhoneHistoryMainArrayModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;
import static nr.king.familytracker.constant.QueryConstants.SELECT_USER_EXPIRY_TIME;
import static nr.king.familytracker.constant.QueryConstants.UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE;

@Repository
public class FilterRepo {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    private static final Logger logger = LogManager.getLogger(FilterRepo.class);

    @Autowired
    private ResponseUtils responseUtils;

    private   FilterHistoryModel localFilterModel;

    public ResponseEntity getFilterData(FilterHistoryModel filterHistoryModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_USER_EXPIRY_TIME,
                    filterHistoryModel.getHomeModel().getId(),filterHistoryModel.getHomeModel().getPackageName());
            if (sqlRowSet.next()) {
                logger.info("Sql has next ");
                if (commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                        filterHistoryModel.getHomeModel().getPackageName(),
                        sqlRowSet.getInt("credit_limit"))) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS " +
                                            "where USER_ID=? and number=? and PACKAGE_NAME=?",
                                    filterHistoryModel.getHomeModel().getId(),
                                    filterHistoryModel.getPhoneNumber(),filterHistoryModel.getHomeModel().getPackageName());
                    if (numberSet.next()) {
                        localFilterModel = new FilterHistoryModel();
                        localFilterModel.setStartDate(filterHistoryModel.getStartDate());
                        localFilterModel.setEndDate(filterHistoryModel.getEndDate());
                        localFilterModel.setPageLimit(400);
                        localFilterModel.setStart(0);
                        localFilterModel.setPhoneNumber(filterHistoryModel.getPhoneNumber());
                        HomeModel homeModel = new HomeModel();
                        homeModel.setId(homeModel.getId());

                        new Thread(()-> makeApiCallFOrUpdatePhoneState(homeModel,numberSet) ).start();

                        HttpResponse httpResponse = httpUtils.doPostRequest(0,
                                GET_HISTORY,
                                commonUtils.getHeadersMapForSpecific(numberSet.getString("TOKEN_HEADER")),
                                "Getting Filter Data",
                                commonUtils.writeAsString(objectMapper, localFilterModel)
                        );
                        GetPhoneHistoryMainArrayModel getPageHistoryNumberModel = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(), GetPhoneHistoryMainArrayModel.class);
                        return responseUtils.constructResponse(200,
                                commonUtils.writeAsString(objectMapper,
                                        new ApiResponse(
                                                httpResponse.getResponseCode() == 200,
                                                (httpResponse.getResponseCode() == 200) ? "The Repose for Filter" : "No Data Available",
                                                getPageHistoryNumberModel
                                        )
                                )
                        );
                    }


                } else {
                    return responseUtils.constructResponse(200, commonUtils.writeAsString(
                            objectMapper,
                            new ApiResponse(false, "Plan is Expired Please Recharge")
                    ));
                }

            }

            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "No User found to Get Filter  History"
                            )));

        } catch (Exception exception) {
            logger.info("Exception in getting filter data " + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }

    }

    private HttpResponse checkUserFromWeTrackService(HomeModel homeModel) throws IOException {
        homeModel.setVersion(VERSION_CODE);
        homeModel.setAppId(APP_ID);
        return httpUtils.doPostRequest(0, CREATE_USER, new HashMap<>(), "Create User",
                commonUtils.writeAsString(objectMapper,
                        homeModel
                ));
    }



    private void makeApiCallFOrUpdatePhoneState(HomeModel homeModel, SqlRowSet numberSet) {
        try{
            HttpResponse httpResponse = httpUtils.doPostRequest(0, GET_APP_USER,
                    commonUtils.getHeadersMap(numberSet.getString("token_header")),
                    "create user Expiry time",
                    commonUtils.writeAsString(objectMapper,
                            new HomeModel(
                            )));

            MainHomeUserModel appUserModel = commonUtils.safeParseJSON(objectMapper,
                    httpResponse.getResponse(),
                    MainHomeUserModel.class);
            if (appUserModel.getData().getFollowings().isEmpty() || !appUserModel.getData().getFollowings().get(0).getIsActive()) {
                logger.info("Update User Profile While getting filter"+commonUtils.writeAsString(objectMapper,appUserModel.getData()));
                HomeModel innerHomeModel = commonUtils.getHomeModel(numberSet.getString("TOKEN_HEADER"), false);
                HttpResponse innerCreateUser = checkUserFromWeTrackService(
                        innerHomeModel
                );

                if (innerCreateUser.getResponseCode() == 200) {
                    PhoneModel phoneModel = new PhoneModel(
                            homeModel.getId(),
                            numberSet.getString("NICK_NAME"),
                            numberSet.getString("NUMBER"),
                            numberSet.getString("COUNTRY_CODE"),
                            numberSet.getString("PUSH_TOKEN"),
                            homeModel.getPackageName()
                    );
                    updateMobileNumbers(phoneModel, innerHomeModel);
                    phoneModel.setId(homeModel.getId());
                    int count = updatePhoneNumberWhileGetAppUser(phoneModel, innerHomeModel);
                    if (count==1)
                    {
                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.setUserId(numberSet.getString("USER_ID"));
                        notificationModel.setHeaderToken(numberSet.getString("TOKEN_HEADER"));
                        notificationModel.setPushToken(numberSet.getString("PUSH_TOKEN"));
                        notificationModel.setNickName(numberSet.getString("NICK_NAME"));
                        notificationModel.setEnable(numberSet.getBoolean("is_noti_enabled"));
                        notificationModel.setNumberId(Long.valueOf(numberSet.getString("NUMBER")));
                        HttpResponse enableSchedularPush =
                                httpUtils.doPostRequest(0,
                                        LOCAL_HOST_NUMBER,
                                        commonUtils.getHeadersMap(numberSet.getString("TOKEN_HEADER")),
                                        "",
                                        commonUtils.writeAsString(objectMapper,notificationModel)
                                );
                        logger.info("enablePush Notification"+enableSchedularPush.getResponseCode());
                    }
                    if (count!=1){
                        logger.info("Unable to update number for user "+phoneModel.getPhoneNumber());
                    }
                }
                else {
                    logger.info("Response while updating the server call " + httpResponse.getResponseCode()+ httpResponse.getResponse());
                }
            }
        }
        catch (Exception exception)
        {
            logger.error("Exception on APi calling"+exception.getMessage(),exception);
        }
    }

    private void updateMobileNumbers(PhoneModel phoneModel, HomeModel homeModel) {
        try {
            phoneModel.setId(homeModel.getId());
            HttpResponse httpResponse = httpUtils.doPostRequest(0,
                    POST_NUMBER,
                    commonUtils.getHeadersMap(phoneModel.getId()),
                    "Update the Phone Number Exiting",
                    commonUtils.writeAsString(objectMapper,
                            phoneModel
                    )
            );
            if (httpResponse.getResponseCode() == 200) {
                logger.info("Response while updating value is" + httpResponse.getResponse());
            }

        } catch (Exception exception) {
            logger.error("Exception in While updating value is" + exception.getMessage());
        }

    }

    private int updatePhoneNumberWhileGetAppUser(PhoneModel phoneModel, HomeModel innerHomeModel) {
        logger.info("Update query is"+commonUtils.writeAsString(objectMapper,phoneModel)+"\n"+
                commonUtils.writeAsString(objectMapper,innerHomeModel));
        return jdbcTemplateProvider.getTemplate()
                .update(UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE,
                        phoneModel.getPhoneNumber(),
                        innerHomeModel.getId(),
                        phoneModel.getCountryCode(),
                        phoneModel.getNickName(),
                        phoneModel.getId(),
                        phoneModel.getPhoneNumber(),
                        phoneModel.getPackageName()
                );
    }


    public ResponseEntity getCompareData(FilterHistoryModel filterHistoryModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_USER_EXPIRY_TIME, filterHistoryModel.getHomeModel().getId(),
                    filterHistoryModel.getHomeModel().getPackageName());
            if (sqlRowSet.next()) {
                List<CommonResponse> commonResponseList =new ArrayList<>();
                if (
                  commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                          filterHistoryModel.getHomeModel().getPackageName(),sqlRowSet.getInt("credit_limit"))

                ) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS " +
                                            "where USER_ID=? and number in (? , ?) and package_name=?",
                                    filterHistoryModel.getHomeModel().getId(),
                                    filterHistoryModel.getPhoneNumber(),
                                    commonUtils.isNullOrEmty(filterHistoryModel.getSecoundNumber()),
                                    filterHistoryModel.getHomeModel().getPackageName()
                            );
                    while (numberSet.next()) {
                         localFilterModel = new FilterHistoryModel();
                        localFilterModel.setStartDate(filterHistoryModel.getStartDate());
                        localFilterModel.setEndDate(filterHistoryModel.getEndDate());
                        localFilterModel.setPageLimit(400);
                        localFilterModel.setStart(0);
                        localFilterModel.setPhoneNumber(numberSet.getString("number"));
                        HttpResponse httpResponse = httpUtils.doPostRequest(0,
                                GET_HISTORY,
                                commonUtils.getHeadersMapForSpecific(numberSet.getString("TOKEN_HEADER")),
                                "Getting Filter Data",
                                commonUtils.writeAsString(objectMapper, localFilterModel)
                        );
                        GetPhoneHistoryMainArrayModel commonResponse = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(), GetPhoneHistoryMainArrayModel.class);
                        logger.info("compare data "+commonUtils.writeAsString(objectMapper,commonResponse));
                        commonResponseList.add(new CommonResponse(commonResponse.getData()));

                    }
                    return responseUtils.constructResponse(200,
                            commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(
                                            !commonResponseList.isEmpty(),
                                            (!commonResponseList.isEmpty()) ? "The Repose for Filter" : "No Data Available",
                                            commonResponseList
                                    )
                            )
                    );


                } else {
                    return responseUtils.constructResponse(200, commonUtils.writeAsString(
                            objectMapper,
                            new ApiResponse(false, "Plan is Expired Please Recharge")
                    ));
                }

            }

            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "No User found to Get Filter  History"
                            )));

        } catch (Exception exception) {
            logger.info("Exception in getting filter data " + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }


    }
}
