package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.migration.DataBaseMigration;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.AppUserModel;
import nr.king.familytracker.model.http.HttpResponse;
import nr.king.familytracker.model.http.PhoneModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.provisioning.UserProvisioning;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
                    .queryForRowSet("select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE from WE_TRACK_USERS where USER_ID=?", homeModel.getId());
            if (sqlRowSet.next()) {
                if (sqlRowSet.getBoolean("IS_USER_CREATED_IN_WETRACK_SERVICE")) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS where USER_ID=?",
                                    homeModel.getId());
                    if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME"))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()) {

                        while (numberSet.next()) {
                            HttpResponse httpResponse = httpUtils.doPostRequest(0, GET_APP_USER,
                                    commonUtils.getHeadersMap(numberSet.getString("TOKEN_HEADER")),
                                    "create user Expiry time",
                                    commonUtils.writeAsString(objectMapper,
                                            new HomeModel(
                                            )));

                            AppUserModel appUserModel = objectMapper.readValue(httpResponse.getResponse(), AppUserModel.class);
                            if (!appUserModel.getFollowings().get(0).getIsActive()) {
                                HomeModel innerHomeModel = commonUtils.getHomeModel(numberSet.getString("TOKEN_HEADER"));
                                HttpResponse innerCreateUser = checkUserFromWeTrackService(
                                        innerHomeModel
                                );

                                if (innerCreateUser.getResponseCode() == 200) {
                                    doUpdateUserCreationinWetrack(innerHomeModel);
                                } else {
                                    logger.info("Response while updating the server call " + httpResponse.getResponseCode(), httpResponse.getResponse());
                                    return responseUtils.constructResponse(200, commonUtils.writeAsString(
                                            objectMapper, new ApiResponse(false, "Unable to Change or Update Phone Number")
                                    ));
                                }
                            }
                        }


                        if (!sqlRowSet.next()) {
                            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                    new ApiResponse(false, "Please add Mobile Number")));
                        }
                    } else {
                        return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                                new ApiResponse(false, "Plan is Expired")));
                    }

                } else {
                    HttpResponse httpResponse = checkUserFromWeTrackService(homeModel);
                    logger.info("Response from inital create" + httpResponse.getResponseCode(), httpResponse.getResponse());
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

            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "No user found ")));
        } catch (Exception exception) {
            logger.error("Exception in  verify user due to is" + exception.getMessage(), exception);
            throw new FailedResponseException("");
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
                                if (httpResponse.getResponseCode()==200)
                                {
                                    int count = updatePhoneNumberDetails(phoneModel);
                                    logger.info("Updated phoneNumber count" + count);
                                    isNumberFound = true;
                                }
                                else{
                                  return addNewNumbeIntoWeTrackServer(phoneModel);
                                }
                            }
                        }

                        if (isNumberFound) {
                            return responseUtils.constructResponse(200,
                                    commonUtils.writeAsString(objectMapper,
                                            new ApiResponse(true,
                                                    "Mobile Number Updated Successfully")));
                        } else {
                          return   addNewNumbeIntoWeTrackServer(phoneModel);
                        }

                    } else {
                        return addNewNumbeIntoWeTrackServer(phoneModel);
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

    private int updatePhoneNumberDetails(PhoneModel phoneModel) {
        return jdbcTemplateProvider.getTemplate()
                .update(UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE, phoneModel.getPhoneNumber(),
                        phoneModel.getCountryCode(),
                        phoneModel.getId(), phoneModel.getPhoneNumber());
    }

    private ResponseEntity addNewNumbeIntoWeTrackServer(PhoneModel phoneModel) throws IOException {
        HomeModel homeModel = commonUtils.getHomeModel(phoneModel.getId());
        HttpResponse httpResponse = httpUtils.doPostRequest(
                0,
                POST_NUMBER,
                commonUtils.getHeadersMap(homeModel.getId()),
                "Create New User for Mobile Creation",
                commonUtils.writeAsString(objectMapper,
                        homeModel)
        );

        if (httpResponse.getResponseCode() == 200) {
            int count = addMobileNumberIntoDatabase(phoneModel, homeModel);

            if (count == 1) {
                return responseUtils.constructResponse(200,
                        commonUtils.writeAsString(objectMapper,
                                new ApiResponse(true, "Phone Number added Successfully")));
            }
        }
        return responseUtils.constructResponse(200,
                commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "Phone Number added UnSuccessfully")));

    }

    private int addMobileNumberIntoDatabase(PhoneModel phoneModel, HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("insert into NUMBER_FOR_USERS (USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE,CREATED_AT,UPDATED_AT) values " +
                                "(?,?,?,?,current_timestamp,current_timestamp)",
                        phoneModel.getId(), phoneModel.getPhoneNumber(),
                        homeModel.getId(), phoneModel.getCountryCode()
                );

    }


    private int doUpdateUserCreationinWetrack(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set IS_USER_CREATED_IN_WETRACK_SERVICE=?,TOKEN_HEADER=?,Expiry_TIME=? where USER_ID=?",
                        true, homeModel.getId(), homeModel.getId(), LocalDateTime.now().plusHours(3).toString());
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
