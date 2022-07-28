package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.*;
import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.model.http.homeModel.GetPhoneHistoryMainArrayModel;
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
import java.util.List;

import static nr.king.familytracker.constant.LocationTrackingConstants.GET_HISTORY;
import static nr.king.familytracker.constant.QueryConstants.SELECT_USER_EXPIRY_TIME;

@Component
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
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_USER_EXPIRY_TIME, filterHistoryModel.getHomeModel().getId());
            if (sqlRowSet.next()) {
                logger.info("Sql has next ");
                if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME"))
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS " +
                                            "where USER_ID=? and number=?",
                                    filterHistoryModel.getHomeModel().getId(),
                                    filterHistoryModel.getPhoneNumber());
                    if (numberSet.next()) {
                         localFilterModel = new FilterHistoryModel();
                        localFilterModel.setStartDate(filterHistoryModel.getStartDate());
                        localFilterModel.setEndDate(filterHistoryModel.getEndDate());
                        localFilterModel.setPageLimit(400);
                        localFilterModel.setStart(0);
                        localFilterModel.setPhoneNumber(filterHistoryModel.getPhoneNumber());

                        HttpResponse httpResponse = httpUtils.doPostRequest(0,
                                GET_HISTORY,
                                commonUtils.getHeadersMapForSpecific(numberSet.getString("TOKEN_HEADER")),
                                "Getting Filter Data",
                                commonUtils.writeAsString(objectMapper, localFilterModel)
                        );
                        GetPhoneHistoryMainArrayModel getPageHistoryNumberModel = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(), GetPhoneHistoryMainArrayModel.class);
                        logger.info("Http responses is"+httpResponse.getResponse());
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

    public ResponseEntity getCompareData(FilterHistoryModel filterHistoryModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_USER_EXPIRY_TIME, filterHistoryModel.getHomeModel().getId());
            if (sqlRowSet.next()) {
                List<CommonResponse> commonResponseList =new ArrayList<>();
                if (System.currentTimeMillis() <= LocalDateTime.parse(sqlRowSet.getString("Expiry_TIME"))
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()) {
                    SqlRowSet numberSet = jdbcTemplateProvider.getTemplate()
                            .queryForRowSet("select USER_ID,NICK_NAME,NUMBER,TOKEN_HEADER,COUNTRY_CODE,PUSH_TOKEN,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS " +
                                            "where USER_ID=? and (number=? or number=?)",
                                    filterHistoryModel.getHomeModel().getId(),
                                    filterHistoryModel.getPhoneNumber(),
                                    filterHistoryModel.getStartHour()
                            );
                    while (numberSet.next()) {
                         localFilterModel = new FilterHistoryModel();
                        localFilterModel.setStartDate(filterHistoryModel.getStartDate());
                        localFilterModel.setEndDate(filterHistoryModel.getEndDate());
                        localFilterModel.setPageLimit(400);
                        localFilterModel.setStart(0);
                        localFilterModel.setPhoneNumber(filterHistoryModel.getPhoneNumber());

                        HttpResponse httpResponse = httpUtils.doPostRequest(0,
                                GET_HISTORY,
                                commonUtils.getHeadersMapForSpecific(numberSet.getString("TOKEN_HEADER")),
                                "Getting Filter Data",
                                commonUtils.writeAsString(objectMapper, localFilterModel)
                        );
                        CommonResponse commonResponse = commonUtils.safeParseJSON(objectMapper, httpResponse.getResponse(), CommonResponse.class);
                        commonResponseList.add(commonResponse);

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
