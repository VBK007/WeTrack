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
import org.springframework.stereotype.Component;

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

@Component
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

    @Transactional
    public ResponseEntity saveUserDetails(HomeModel homeModel) {
        try {
            int count = doUpdateUser(homeModel);
            if (count == 0) {
                count = createUser(homeModel);
                if (count == 1) dataBaseMigration.createSchema(homeModel.getId());
            }
            String authToken = "";

            if (count == 1) {
                authToken = UUID.randomUUID().toString();
                doUpdateTokenforUser(authToken, homeModel);
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

    private void doUpdateTokenforUser(String authToken, HomeModel homeModel) {
        int count = jdbcTemplateProvider.getTemplate()
                .update("update AUTH_TOKEN set AUTH_TOKEN=?,UPDATED_AT=current_timestamp   where USER_ID=?", authToken, homeModel.getId());
        if (count == 0) {
            count = jdbcTemplateProvider.getTemplate()
                    .update("insert into AUTH_TOKEN (USER_ID,AUTH_TOKEN,CREATED_AT,UPDATED_AT) values (?,?,current_timestamp,current_timestamp)",
                            homeModel.getId(), authToken);
        }
        logger.info("count for auth token" + count);

    }

    @Transactional
    public ResponseEntity verify_user(HomeModel homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet("select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE,token_header  from WE_TRACK_USERS where USER_ID=?", homeModel.getId());


            if (sqlRowSet.next()) {
                logger.info("outside Event " + homeModel.getId());
                if (sqlRowSet.getBoolean("IS_USER_CREATED_IN_WETRACK_SERVICE")) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS where USER_ID=?",
                                    homeModel.getId());
                    if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME"))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()) {
                        if (!numberSet.next()) {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "Please add Mobile Number")));
                        }
                        while (numberSet.next()) {
                            HttpResponse httpResponse = httpUtils.doPostRequest(0, GET_APP_USER,
                                    commonUtils.getHeadersMap(numberSet.getString("token_header")),
                                    "create user Expiry time",
                                    commonUtils.writeAsString(objectMapper,
                                            new HomeModel(
                                            )));
                            MainHomeUserModel appUserModel = commonUtils.safeParseJSON(objectMapper,
                                    httpResponse.getResponse(),
                                    MainHomeUserModel.class);
                            if (appUserModel.getData().getFollowings().isEmpty()||!appUserModel.getData().getFollowings().get(0).getIsActive()) {
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
                                            numberSet.getString("PUSH_TOKEN")
                                    );
                                    updateMobileNumbers(phoneModel, innerHomeModel);
                                    phoneModel.setId(homeModel.getId());
                                    int count = updatePhoneNumberWhileGetAppUser(phoneModel, innerHomeModel);
                                    logger.info("Update count " + count);
                                    if (count!=1){
                                        return responseUtils.constructResponse(200, commonUtils.writeAsString(
                                                objectMapper, new ApiResponse(false, "Unable to Change or Update Phone Number")
                                        ));
                                    }
                                }
                                else {
                                    logger.info("Response while updating the server call " + httpResponse.getResponseCode(), httpResponse.getResponse());
                                    return responseUtils.constructResponse(200, commonUtils.writeAsString(
                                            objectMapper, new ApiResponse(false, "Unable to Change or Update Phone Number")
                                    ));
                                }
                            }
                        }
                    }
                    else {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Plan is Expired")));
                    }

                } else {
                    HttpResponse httpResponse = checkUserFromWeTrackService(homeModel);
                    if (httpResponse.getResponseCode() == 200) {
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
            else{
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
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME, phoneModel.getId());

            if (sqlRowSet.next()) {
                if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME"))
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()) {
                    SqlRowSet innerMobileRowSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet(selectNumberWithToken, phoneModel.getId());
                    if (innerMobileRowSet.next()) {
                        boolean isNumberFound = false;
                        while (innerMobileRowSet.next()) {
                            if (phoneModel.getPhoneNumber().equals(innerMobileRowSet.getString("NUMBER"))) {
                                HttpResponse httpResponse = httpUtils.doPostRequest(0,
                                        POST_NUMBER,
                                        commonUtils.getHeadersMap(innerMobileRowSet.getString("TOKEN_HEADER")),
                                        "Update the Phone Number Exiting",
                                        commonUtils.writeAsString(objectMapper,
                                                phoneModel
                                        )
                                );
                                if (httpResponse.getResponseCode() == 200) {
                                    int count = updatePhoneNumberDetails(phoneModel);
                                    logger.info("Updated phoneNumber count" + count);
                                    isNumberFound = true;
                                } else {
                                    return addNewNumbeIntoWeTrackServer(phoneModel, false);
                                }
                            }
                        }

                        if (isNumberFound) {
                            return responseUtils.constructResponse(200,
                                    commonUtils.writeAsString(objectMapper,
                                            new ApiResponse(true,
                                                    "Mobile Number Updated Successfully")));
                        } else {
                            return addNewNumbeIntoWeTrackServer(phoneModel, false);
                        }

                    } else {
                        return addNewNumbeIntoWeTrackServer(phoneModel, true);
                    }

                }

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
        logger.info("Update query is"+commonUtils.writeAsString(objectMapper,phoneModel)+"\n"+
                commonUtils.writeAsString(objectMapper,innerHomeModel));
        return jdbcTemplateProvider.getTemplate()
                .update(UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE,
                        phoneModel.getPhoneNumber(),
                        innerHomeModel.getId(),
                        phoneModel.getCountryCode(),
                        phoneModel.getId(),
                        phoneModel.getPhoneNumber());
    }

    private int updatePhoneNumberDetails(PhoneModel phoneModel) {
        return jdbcTemplateProvider.getTemplate()
                .update(UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE,
                        phoneModel.getPhoneNumber(),
                        phoneModel.getCountryCode(),
                        phoneModel.getId(), phoneModel.getPhoneNumber());
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
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(true, "Phone Number added Successfully")));
        }
        return responseUtils.constructResponse(200,
                commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "Phone Number added UnSuccessfully")));
    }

    private int addMobileNumberIntoDatabase(PhoneModel phoneModel, HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("insert into NUMBER_FOR_USERS (USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE,CREATED_AT,UPDATED_AT,NICK_NAME,PUSH_TOKEN,EXPIRY_TIME) values " +
                                "(?,?,?,?,current_timestamp,current_timestamp,?,?,?)",
                        phoneModel.getId(), phoneModel.getPhoneNumber(),
                        homeModel.getId(), phoneModel.getCountryCode(),
                        phoneModel.getNickName(), phoneModel.getPushToken(),
                        LocalDateTime.now().plusHours(3).toString()
                );

    }


    public ResponseEntity getAllMobileNumbers(PhoneModel phoneModel) {
        try {
            List<GetMobileResponse> getMobileResponses = new ArrayList<>();
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(GET_ALL_MOBILE_NUMBER, phoneModel.getId());
            while (sqlRowSet.next()) {
                GetMobileResponse getMobileResponse = new GetMobileResponse();
                getMobileResponse.setMobileNumber(sqlRowSet.getString("NUMBER"));
                getMobileResponse.setCountryCode(sqlRowSet.getString("COUNTRY_CODE"));
                getMobileResponse.setExpiryTime(sqlRowSet.getString("EXPIRY_TIME"));
                getMobileResponse.setNickName(sqlRowSet.getString("NICK_NAME"));
                getMobileResponse.setPushToken(sqlRowSet.getString("PUSH_TOKEN"));
                getMobileResponse.setExpiryToken(sqlRowSet.getString("TOKEN_HEADER"));
                getMobileResponse.setUserId(sqlRowSet.getString("USER_ID"));
                getMobileResponses.add(getMobileResponse);
            }

            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(getMobileResponses.isEmpty(), "The Mobile Numbers are", getMobileResponses))
            );


        } catch (Exception exception) {
            logger.error("Exception in getting MobileNumber due to", exception.getMessage());
            throw new FailedResponseException("");
        }
    }


    private int doUpdateUserCreationinWetrack(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set IS_USER_CREATED_IN_WETRACK_SERVICE=?,TOKEN_HEADER=?,Expiry_TIME=? where USER_ID=?",
                        true, homeModel.getId(), LocalDateTime.now().plusHours(3).toString(), homeModel.getId());
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
                        "CREATED_AT,UPDATED_AT,IS_USER_CREATED_IN_WETRACK_SERVICE,TOKEN_HEADER,IS_NUMBER_ADDER,SCHEMA_NAME) " +
                        "values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp,?,?,?,?)",
                homeModel.getId(), homeModel.getPhoneModel(), homeModel.getIpAddress(), homeModel.getCountryName(),
                homeModel.getOneSignalExternalUserId(), homeModel.getAppId(), LocalDateTime.now().plusHours(3).toString(), false, false,
                "", false, "we_tracker" + homeModel.getId()
        );
    }

    private int doUpdateUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set USER_ID=?,MOBILE_MODEL=?,IP_ADDRESS=?,COUNTRY=?," +
                                "ONE_SIGNAL_EXTERNAL_USERID=?,MOBILE_VERSION=?,UPDATED_AT = current_timestamp" +
                                " where USER_ID=?", homeModel.getId(), homeModel.getPhoneModel(), homeModel.getIpAddress(),
                        homeModel.getCountryName(), homeModel.getOneSignalExternalUserId(), homeModel.getAppId(),
                        homeModel.getId()
                );
    }


}
