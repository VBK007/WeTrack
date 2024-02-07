package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.*;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.provisioning.UserProvisioning;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;
import static nr.king.familytracker.constant.QueryConstants.*;

@Repository
@EnableAutoConfiguration
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

    @Autowired
    UserProvisioning dataBaseMigration;


    @Autowired
    private GetHistoryRepo getHistoryRepo;

    NotificationModel notificationModel = new NotificationModel();

    @Transactional
    public ResponseEntity saveUserDetails(HomeModel homeModel) {
        try {
            int count = doUpdateUser(homeModel);
            if (count == 1) {
                new Thread(() -> updateTokenForUser(homeModel.getId(), homeModel.getOneSignalExternalUserId())).start();
            }
            if (count == 0) {
                count = createUser(homeModel);//createUserInital
                if (count == 1) {
                    //dataBaseMigration.createSchema(homeModel.getId());
                    doUploadtoSchedularFunction(homeModel);
                }
            }
            String authToken = "";
            if (count == 1) {
                authToken = UUID.randomUUID().toString();
               // doUpdateTokenforUser(authToken, homeModel); //update auth token
                doandCreateLoginNumberOfTime(homeModel);
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

    private void updateTokenForUser(String id, String oneSignalExternalUserId) {
        try {
            NotificationModel dummyModel = new NotificationModel();
            notificationModel.setUserId(id);
            notificationModel.setPushToken(oneSignalExternalUserId);
            HttpResponse enableSchedularPush =
                    httpUtils.doPostRequest(0,
                            LOCAL_HOST_TOKEN,
                            commonUtils.getHeadersMap(id),
                            "",
                            commonUtils.writeAsString(objectMapper, dummyModel)
                    );
            logger.info("enablePush Notification" + enableSchedularPush.getResponseCode());
        } catch (Exception exception) {
            logger.info("Exception in updating token in another server" + exception.getMessage(), exception);
        }
    }

    private void doUploadtoSchedularFunction(HomeModel homeModel) {
        try {
            HttpResponse sendNewUsertoSchedular =
                    httpUtils.doPostRequest(
                            0, LOCAL_HOST_ADD_USER,
                            new HashMap<>(), "",
                            commonUtils.writeAsString(objectMapper, homeModel)
                    );

            logger.info("Creating new User in Schedular server " + sendNewUsertoSchedular.getResponseCode());
        } catch (Exception exception) {
            logger.error("Exception in sending new user to scheduler server" + exception.getMessage(), exception);
        }
    }

    private void doUpdateTokenforUser(String authToken, HomeModel homeModel) {
        int count = jdbcTemplateProvider.getTemplate()
                .update("update AUTH_TOKEN set AUTH_TOKEN=?,UPDATED_AT=current_timestamp   where USER_ID=? and package_name=?",
                        authToken, homeModel.getId(),
                        homeModel.getPackageName());
        if (count == 0) {
            count = jdbcTemplateProvider.getTemplate()
                    .update("insert into AUTH_TOKEN (USER_ID,AUTH_TOKEN,CREATED_AT,UPDATED_AT,PACKAGE_NAME) values (?,?,current_timestamp,current_timestamp,?)",
                            homeModel.getId(), authToken, homeModel.getPackageName());
        }
        logger.info("count for auth token" + count);
    }

    private int updateAccountDetails(String userId, String packageName) {
        return jdbcTemplateProvider.getTemplate().update(UPDATE_PURCHASE_MODE, userId, packageName);
    }

    public ResponseEntity verifyAddUser(HomeModel homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet("select Expiry_TIME,CREDIT_LIMIT,IS_USER_CREATED_IN_WETRACK_SERVICE,token_header,purchase_mode,PACKAGE_NAME  " +
                            "from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?", homeModel.getId(), homeModel.getPackageName());
            if (sqlRowSet.next()) {
                logger.info("inside Event " + homeModel.getId() + sqlRowSet.getBoolean("is_user_created_in_wetrack_service"));
                if (sqlRowSet.getBoolean("is_user_created_in_wetrack_service")) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT,is_noti_enabled from NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?",
                                    homeModel.getId(), homeModel.getPackageName());
                    if (40 <= LocalDateTime.parse(sqlRowSet.getString("CREDIT_LIMIT"))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()) {
                        if (!numberSet.next()) {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "Please add Mobile Number")));
                        }
                        new Thread(() -> doInBakground(homeModel, numberSet)).start();
                    } else {
                        int updatedUserCount = updateAccountDetails(sqlRowSet.getString("user_id"),sqlRowSet.getString("PACKAGE_NAME"));
                        logger.error("Response in the Update user into demo for user "+sqlRowSet.getString("user_id")+"\n response count"
                                +updatedUserCount);
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Plan is Expired")));
                    }
                }
                else {
                    String normalUserId = homeModel.getId();
                    homeModel.setId(commonUtils.getRandomString());
                    HttpResponse httpResponse = checkUserFromWeTrackService(homeModel);
                    if (httpResponse.getResponseCode() == 200) {
                        homeModel.setId(normalUserId);
                        int count = doUpdateUserCreationinWetrack(homeModel);
                        logger.info("Count of update is" + count);
                        if (count == 1) {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new
                                    ApiResponse(true,
                                    "User Created Successfully")));
                        } else {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "User Created Unsuccessfully")));
                        }
                    } else {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new
                                ApiResponse(false,
                                "User Created UnSuccessfully")));
                    }
                }
            } else {
                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "No user found ")));

            }

            logger.info("Last Response ");
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(true, "User number Updated ")));

        } catch (Exception exception) {
            logger.error("Exception in  verify  Add user due to is" + exception.getMessage(), exception);
            throw new FailedResponseException("");
        }
    }


    public ResponseEntity getUserNeed(HomeModel homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet("select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE,token_header,CREDIT_LIMIT  " +
                            "from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?", homeModel.getId(), homeModel.getPackageName());
            if (sqlRowSet.next()) {
                SqlRowSet innerNumberSet = jdbcTemplateProvider.getTemplate()
                        .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT,is_noti_enabled from " +
                                        "NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?",
                                homeModel.getId(), homeModel.getPackageName());
                if (commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                        homeModel.getPackageName(),
                        sqlRowSet.getInt("CREDIT_LIMIT"))) {

                    if (innerNumberSet.wasNull()) {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Please add Mobile Number")));
                    }

                    new Thread(() -> doInBakground(homeModel, innerNumberSet)).start();
                }
                else {
                    return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "Plan is Expired")));
                }
            }
            else {
                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "No user found ")));
            }
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(true, "User Updated ")));
        } catch (Exception exception) {
            logger.error("Exception in  GetUser need due to is" + exception.getMessage(), exception);
            throw new FailedResponseException("");
        }
    }


    @Transactional
    public ResponseEntity verify_user(HomeModel homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet("select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE,token_header,CREDIT_LIMIT  " +
                            "from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?", homeModel.getId(), homeModel.getPackageName());

            if (sqlRowSet.next()) {
                logger.info("outside Event " + homeModel.getId() + sqlRowSet.getBoolean("is_user_created_in_wetrack_service"));
                if (sqlRowSet.getBoolean("is_user_created_in_wetrack_service")) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT,is_noti_enabled from NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?",
                                    homeModel.getId(), homeModel.getPackageName());
                    if (commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"), homeModel.getPackageName(),
                            sqlRowSet.getInt("CREDIT_LIMIT"))) {
                        if (!numberSet.next()) {
                            return responseUtils.constructResponse(200,
                                    commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "Please add Mobile Number")));
                        }
                        new Thread(() -> doInBakground(homeModel, numberSet)).start();
                    } else {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Plan is Expired")));
                    }
                } else {
                    String normalUserId = homeModel.getId();
                    homeModel.setId(commonUtils.getRandomString());
                    HttpResponse httpResponse = checkUserFromWeTrackService(homeModel);
                    if (httpResponse.getResponseCode() == 200) {
                        homeModel.setId(normalUserId);
                        int count = doUpdateUserCreationinWetrack(homeModel);
                        logger.info("Count of update is" + count);
                        if (count == 1) {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new
                                    ApiResponse(true,
                                    "User Created Successfully")));
                        } else {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "User Created Unsuccessfully")));
                        }
                    } else {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new
                                ApiResponse(false,
                                "User Created UnSuccessfully")));
                    }
                }
            }
            else {
                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "No user found ")));

            }

            logger.info("Last Response ");
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(true, "User number Updated ")));

        } catch (Exception exception) {
            logger.error("Exception in  verify user due to is" + exception.getMessage(), exception);
            throw new FailedResponseException("");
        }
    }

    private void doInBakground(HomeModel homeModel, SqlRowSet numberSet) {
        try {
            if (numberSet.isLast()) {
                new Thread(() -> makeApiCallFOrUpdatePhoneState(homeModel, numberSet)).start();
            } else {
                while (numberSet.next()) {
                    new Thread(() -> makeApiCallFOrUpdatePhoneState(homeModel, numberSet)).start();
                    Thread.sleep(500, 50);
                }
            }
        } catch (Exception exception) {
            logger.error("Exception while updating number for User" + homeModel.getId() + "||" + exception.getMessage(), exception);
        }
    }


    private void makeApiCallFOrUpdatePhoneState(HomeModel homeModel, SqlRowSet numberSet) {
        try {
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
                    updateMobileNumbers(phoneModel, innerHomeModel); //api call for update phone number
                    phoneModel.setId(homeModel.getId());
                    int count = updatePhoneNumberWhileGetAppUser(phoneModel, innerHomeModel);
                    if (count == 1) {
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
                                        commonUtils.writeAsString(objectMapper, notificationModel)
                                );
                        logger.info("enablePush Notification" + enableSchedularPush.getResponseCode());
                        logger.info("Push Notification data is"+ notificationModel.getNumberId());
                    }
                    if (count != 1) {
                        logger.info("Unable to update number for user " + phoneModel.getPhoneNumber());
                    }
                } else {
                    logger.info("Response while updating the server call " + httpResponse.getResponseCode() + httpResponse.getResponse());
                }
            }
        } catch (Exception exception) {
            logger.error("Exception on APi calling" + exception.getMessage(), exception);
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


    @Transactional
    public ResponseEntity addMobileNumber(PhoneModel phoneModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME_WITH_ACCOUNT_DETAILS, phoneModel.getId(), phoneModel.getPackageName());
            SqlRowSet countSet = jdbcTemplateProvider.getTemplate().queryForRowSet("select count(*) from NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?",
                    phoneModel.getId(), phoneModel.getPackageName());

            if (sqlRowSet.next()) {
                if (
                        commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"), phoneModel.getPackageName(),
                                sqlRowSet.getInt("CREDIT_LIMIT"))
                ) {

                    SqlRowSet innerMobileRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(selectNumberWithToken, phoneModel.getId(), phoneModel.getPackageName());
                    if (innerMobileRowSet.next()) {
                        boolean isNumberFound = false;
                        while (innerMobileRowSet.next()) {
                            if (phoneModel.getPhoneNumber().equals(innerMobileRowSet.getString("NUMBER"))) {
                                int count = updatePhoneNumberDetails(phoneModel);
                                isNumberFound = true;
                            }
                        }
                        if (isNumberFound) {
                            return responseUtils.constructResponse(200,
                                    commonUtils.writeAsString(objectMapper,
                                            new ApiResponse(true,
                                                    "Mobile Number Updated Successfully")));
                        } else {
                            if (countSet.next() && countSet.getInt("Count") < sqlRowSet.getInt("max_number")) {
                                return addNewNumbeIntoWeTrackServer(phoneModel, false);
                            } else {
                                return responseUtils.constructResponse(200, commonUtils.writeAsString(
                                        objectMapper, new ApiResponse(false, "Unable to add New Number Recharge the Plan!!")
                                ));
                            }
                        }

                    } else {
                        if (countSet.next() && countSet.getInt("Count") < sqlRowSet.getInt("max_number")) {
                            return addNewNumbeIntoWeTrackServer(phoneModel, false);
                        } else {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(
                                    objectMapper, new ApiResponse(false, "Unable to add New Number Recharge the Plan!!")
                            ));
                        }
                    }

                }


                return responseUtils.constructResponse(200,
                        commonUtils.writeAsString(objectMapper, new ApiResponse(false, "Unable to add Number Please Recharge the Plan")));

            }
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "No User found to Create"
                            )));

        } catch (Exception exception) {
            logger.error("Exception in adding Number due to" + exception.getMessage(), exception);
            throw new FailedResponseException("");
        }
    }


    private int updatePhoneNumberWhileGetAppUser(PhoneModel phoneModel, HomeModel innerHomeModel) {
        logger.info("Update query is" + commonUtils.writeAsString(objectMapper, phoneModel) + "\n" +
                commonUtils.writeAsString(objectMapper, innerHomeModel));
        return jdbcTemplateProvider.getTemplate()
                .update(UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE,
                        phoneModel.getPhoneNumber(),
                        innerHomeModel.getId(),
                        phoneModel.getCountryCode(),
                        phoneModel.getNickName(),
                        phoneModel.getId(),
                        phoneModel.getPhoneNumber(),
                        phoneModel.getPackageName());
    }

    private int updatePhoneNumberDetails(PhoneModel phoneModel) {
        return jdbcTemplateProvider.getTemplate()
                .update(UPDATE_DETAILS_IN_NUMBER_MOBILE,
                        phoneModel.getPhoneNumber(),
                        phoneModel.getCountryCode(),
                        phoneModel.getNickName(),
                        phoneModel.getId(), phoneModel.getPhoneNumber(),
                        phoneModel.getPackageName()
                );
    }

    private ResponseEntity addNewNumbeIntoWeTrackServer(PhoneModel phoneModel, boolean isFirstTime) throws IOException {
        HomeModel homeModel = commonUtils.getHomeModel(phoneModel.getId(), isFirstTime);

        if (isFirstTime) {
            HttpResponse httpResponse = httpUtils.doPostRequest(
                    0,
                    POST_NUMBER,
                    commonUtils.getHeadersMap(phoneModel.getId()),
                    "Adding Number for  Mobile Creation",
                    commonUtils.writeAsString(objectMapper,
                            phoneModel)
            );
            logger.info("Response in adding number" + httpResponse.getResponse() + "\n" + httpResponse.getResponse());

            if (httpResponse.getResponseCode() == 200) {
                HomeModel innerModel = new HomeModel();
                innerModel.setId(phoneModel.getId());
                return addMobileNumbers(phoneModel, innerModel);
            }
        } else {
            HttpResponse httpResponse = httpUtils.doPostRequest(
                    0,
                    CREATE_USER,
                    commonUtils.getHeadersMap(homeModel.getId()),
                    "Create New User for Mobile Creation",
                    commonUtils.writeAsString(objectMapper,
                            homeModel)
            );
            logger.info("Response in adding number" + httpResponse.getResponse() + "\n" + httpResponse.getResponse());

            if (httpResponse.getResponseCode() == 200) {
                HttpResponse innerMobileRequest = httpUtils.doPostRequest(0,
                        POST_NUMBER,
                        commonUtils.getHeadersMap(homeModel.getId()),
                        "Adding Number for  Mobile Creation",
                        commonUtils.writeAsString(objectMapper,
                                phoneModel
                        ));
                if (innerMobileRequest.getResponseCode() == 200) {
                    return addMobileNumbers(phoneModel, homeModel);
                }
            }
        }


        return responseUtils.constructResponse(200,
                commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "Phone Number added UnSuccessfully")));

    }

    private ResponseEntity addMobileNumbers(PhoneModel phoneModel, HomeModel homeModel) {
        int count = addMobileNumberIntoDatabase(phoneModel, homeModel);
        if (count == 1) {
            notificationModel.setUserId(phoneModel.getId());
            notificationModel.setHeaderToken(homeModel.getId());
            notificationModel.setPushToken(phoneModel.getPushToken());
            notificationModel.setNickName(phoneModel.getNickName());
            notificationModel.setEnable(true);
            notificationModel.setNumberId(Long.valueOf(phoneModel.getPhoneNumber()));
            new Thread(() -> doNotifyInNewServer(notificationModel, homeModel.getId())).start();
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(true, "Phone Number added Successfully")));
        }
        return responseUtils.constructResponse(200,
                commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "Phone Number added UnSuccessfully")));
    }

    private void doNotifyInNewServer(NotificationModel notificationModel, String id) {
        try {
            HttpResponse enableSchedularPush =
                    httpUtils.doPostRequest(0,
                            LOCAL_HOST_NUMBER,
                            commonUtils.getHeadersMap(id),
                            "",
                            commonUtils.writeAsString(objectMapper, notificationModel)
                    );
            logger.info("enablePush Notification" + enableSchedularPush.getResponseCode());
        } catch (Exception exception) {
            logger.info("Exception error while sending user number " + exception.getMessage(), exception);
        }
    }

    private int addMobileNumberIntoDatabase(PhoneModel phoneModel, HomeModel homeModel) {
        //"+String.format("%s%s",WETRACK,phoneModel.getId())+".
        return jdbcTemplateProvider.getTemplate()
                .update("insert into NUMBER_FOR_USERS (USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE," +
                                "CREATED_AT,UPDATED_AT,NICK_NAME,PUSH_TOKEN,EXPIRY_TIME,is_noti_enabled,PACKAGE_NAME) values " +
                                "(?,?,?,?,current_timestamp,current_timestamp,?,?,?,?,?)",
                        phoneModel.getId(), phoneModel.getPhoneNumber(),
                        homeModel.getId(), phoneModel.getCountryCode(),
                        phoneModel.getNickName(), phoneModel.getPushToken(),
                        LocalDateTime.now().plusHours(3).toString(),
                        true,
                        phoneModel.getPackageName()
                );

    }


    public ResponseEntity getAllMobileNumbers(PhoneModel phoneModel) {
        try {
            List<GetMobileResponse> getMobileResponses = new ArrayList<>();
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(GET_ALL_MOBILE_NUMBER, phoneModel.getId(), phoneModel.getPackageName());
            while (sqlRowSet.next()) {
                GetMobileResponse getMobileResponse = new GetMobileResponse();
                getMobileResponse.setMobileNumber(sqlRowSet.getString("NUMBER"));
                getMobileResponse.setCountryCode(sqlRowSet.getString("COUNTRY_CODE"));
                //getMobileResponse.setExpiryTime(sqlRowSet.getString("EXPIRY_TIME"));
                getMobileResponse.setNickName(sqlRowSet.getString("NICK_NAME"));
                //getMobileResponse.setPushToken(sqlRowSet.getString("PUSH_TOKEN"));
                //getMobileResponse.setExpiryToken(sqlRowSet.getString("TOKEN_HEADER"));
                getMobileResponse.setUserId(sqlRowSet.getString("USER_ID"));
                getMobileResponses.add(getMobileResponse);
            }

            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(!getMobileResponses.isEmpty(), "The Mobile Numbers are", getMobileResponses))
            );


        } catch (Exception exception) {
            logger.error("Exception in getting MobileNumber due to", exception.getMessage());
            throw new FailedResponseException("");
        }
    }


    private int doUpdateUserCreationinWetrack(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set IS_USER_CREATED_IN_WETRACK_SERVICE=?,TOKEN_HEADER=? where USER_ID=? and PACKAGE_NAME=?",
                        true, homeModel.getId(), homeModel.getId(), homeModel.getPackageName());
    }

    private HttpResponse checkUserFromWeTrackService(HomeModel homeModel) throws IOException {
        homeModel.setVersion(VERSION_CODE);
        homeModel.setAppId(APP_ID);
        return httpUtils.doPostRequest(0, CREATE_USER, new HashMap<>(), "Create User",
                commonUtils.writeAsString(objectMapper,
                        homeModel
                ));
    }


    private int createUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate().update("insert into WE_TRACK_USERS " +
                        "(USER_ID,MOBILE_MODEL,IP_ADDRESS,COUNTRY,ONE_SIGNAL_EXTERNAL_USERID,MOBILE_VERSION,Expiry_TIME,IS_PURCHASED," +
                        "CREATED_AT,UPDATED_AT,IS_USER_CREATED_IN_WETRACK_SERVICE,TOKEN_HEADER,IS_NUMBER_ADDER,SCHEMA_NAME,purchase_mode,MAX_NUMBER,PACKAGE_NAME,CREDIT_LIMIT) " +
                        "values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp,?,?,?,?,?,?,?,?)",
                homeModel.getId(), homeModel.getPhoneModel(), homeModel.getIpAddress(), homeModel.getCountryName(),
                homeModel.getOneSignalExternalUserId(), homeModel.getAppId(),
                (getAppStatus()) ? LocalDateTime.now().plusHours(48).toString() :
                        LocalDateTime.now().plusYears(1).toString(), false, false,
                "", false, WETRACK + homeModel.getId(), commonUtils.getModel(homeModel.getPackageName()), 1, homeModel.getPackageName(),
                100
        );
    }

    private boolean getAppStatus() {
        SqlRowSet appVersionSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_APP_FLOW);
        return  appVersionSet.next() && appVersionSet.getBoolean("IS_FREE_MODE");
    }


    private int doandCreateLoginNumberOfTime(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate().update("insert into WE_TRACK_USERS_NO_OF_LOGIN " +
                        "(USER_ID,MOBILE_MODEL,IP_ADDRESS,COUNTRY,ONE_SIGNAL_EXTERNAL_USERID,MOBILE_VERSION,Expiry_TIME,IS_PURCHASED," +
                        "CREATED_AT,UPDATED_AT,IS_USER_CREATED_IN_WETRACK_SERVICE,TOKEN_HEADER,IS_NUMBER_ADDER,SCHEMA_NAME,PACKAGE_NAME,CREDIT_LIMIT,DAY) " +
                        "values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp,?,?,?,?,?,?,?)",
                homeModel.getId(), homeModel.getPhoneModel(), homeModel.getIpAddress(), homeModel.getCountryName(),
                homeModel.getOneSignalExternalUserId(), homeModel.getAppId(), LocalDateTime.now().plusHours(23).toString(), false, false,
                "", false, WETRACK + homeModel.getId(), homeModel.getPackageName(),
                100,
                LocalDateTime.now().toLocalDate().toString()
        );
    }

    private int doUpdateUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set USER_ID=?,MOBILE_MODEL=?,IP_ADDRESS=?,COUNTRY=?," +
                                "ONE_SIGNAL_EXTERNAL_USERID=?,MOBILE_VERSION=?,UPDATED_AT = current_timestamp" +
                                " where USER_ID=? and package_name=?", homeModel.getId(), homeModel.getPhoneModel(), homeModel.getIpAddress(),
                        homeModel.getCountryName(), homeModel.getOneSignalExternalUserId(), homeModel.getAppId(),
                        homeModel.getId(), homeModel.getPackageName()
                );
    }


}
