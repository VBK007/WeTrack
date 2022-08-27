package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public class UpdateAddControllerRepo {

    @Autowired
    private ResponseUtils responseUtils;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;



    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;


    private static final Logger logger = LogManager.getLogger(UpdateAddControllerRepo.class);

    @Transactional
    public ResponseEntity saveUserDetails(HomeModel homeModel) {
        try {
            int count = doUpdateUser(homeModel);
            return responseUtils.constructResponse(200,
                    commonUtils.writeAsString(objectMapper,
                            new ApiResponse(count == 1, (count == 1) ? "The add Values is Updated" : "Unable to update add Value")));

        } catch (Exception exception) {
            logger.error("Exception in saveuser Details" + exception.getMessage(),
                    exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    private int doUpdateUser(HomeModel homeModel) {
        return jdbcTemplateProvider.getTemplate()
                .update("update WE_TRACK_USERS set USER_ID=?,MOBILE_MODEL=?,IP_ADDRESS=?,COUNTRY=?," +
                                "ONE_SIGNAL_EXTERNAL_USERID=?,MOBILE_VERSION=?,UPDATED_AT = current_timestamp,PACKAGE_NAME=?,CREDIT_LIMIT=? " +
                                " where USER_ID=? and PACKAGE_NAME=?", homeModel.getId(), homeModel.getPhoneModel(), homeModel.getIpAddress(),
                        homeModel.getCountryName(), homeModel.getOneSignalExternalUserId(), homeModel.getAppId(),
                        homeModel.getPackageName(), getNumberValues(homeModel.getId(), homeModel.getPackageName(), homeModel.isAdding()),
                        homeModel.getId(),
                        homeModel.getPackageName()
                );
    }

    private int getNumberValues(String id, String packageName, boolean isAdding) {

        int numberValue = 0;
        SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(
                "select CREDIT_LIMIT from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?",
                id, packageName);
        if (sqlRowSet.next()) {
            numberValue = sqlRowSet.getInt("CREDIT_LIMIT");
        }
        numberValue = (isAdding) ? numberValue + 20 : numberValue - 20;
        return numberValue;
    }

}
