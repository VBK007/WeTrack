package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
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

import java.util.Arrays;

import static nr.king.familytracker.constant.LocationTrackingConstants.PACKAGE_ARRAY_WITHOUT_ADD;
import static nr.king.familytracker.constant.QueryConstants.SELECT_USER_EXPIRY_TIME;

@Repository
public class ContactUserRepo {
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

    private static final Logger logger = LogManager.getLogger(ContactUserRepo.class);


    public ResponseEntity getInstaPackageName(HomeModel getPhoneHistoryModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate()
                    .queryForRowSet(SELECT_USER_EXPIRY_TIME,getPhoneHistoryModel.getId(),getPhoneHistoryModel.getPackageName());
            if (sqlRowSet.next()) {
                if (commonUtils.checkAddOrWithoutAdd(sqlRowSet.getString("Expiry_TIME"),
                        getPhoneHistoryModel.getPackageName(),
                        sqlRowSet.getInt("credit_limit")))
                {
                  if (Arrays.asList(PACKAGE_ARRAY_WITHOUT_ADD).contains(getPhoneHistoryModel.getIpAddress()))
                  {
                      return responseUtils.constructResponse(200,commonUtils.writeAsString(objectMapper,
                              new ApiResponse(true,"Getting User","familytracking/")));
                  }
                }
                else{
                    return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                            new ApiResponse(false, "Plan is Expired")));
                }
            }
                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                        new ApiResponse(false, "No User Found")));

        } catch (Exception exception) {
            logger.error("Exception in  verify user due to is" + exception.getMessage(), exception);
            throw new FailedResponseException("");
        }
    }

}
