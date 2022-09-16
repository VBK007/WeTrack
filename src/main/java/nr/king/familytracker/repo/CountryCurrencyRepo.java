package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.currency.CurrecyModel;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import static nr.king.familytracker.constant.QueryConstants.INSERT_COUNTRY_CURRENCY;
import static nr.king.familytracker.constant.QueryConstants.UPDATE_NUMBER_CURRENCY;

@Repository
public class CountryCurrencyRepo {

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

    public ResponseEntity insertAppCountryValues(CurrecyModel filterHistoryModel) {
        try {
            boolean isLast =false;

          for (int i=0;i<filterHistoryModel.getListofCountry().size();i++)
          {
              CurrecyModel.ListofCountry lData = filterHistoryModel.getListofCountry().get(i);
             int count = jdbcTemplateProvider.getTemplate().update(UPDATE_NUMBER_CURRENCY,
                     lData.getSymbol(),lData.getCode(),lData.getCountryCode(),lData.getDecimal_digits(),
                     lData.getRounding(),lData.getMoneyOneDay(),lData.getMoneyOneWeek(),lData.getMoneyOneMonth(),
                     lData.getMoneyThreeMonth(),lData.getMoneyOneYear(),lData.getId());

             if (count==0)
             {
                 count = jdbcTemplateProvider.getTemplate()
                         .update(INSERT_COUNTRY_CURRENCY,
                                 lData.getSymbol(),lData.getCode(),
                                 lData.getSymbol_native(),
                                 lData.getCountryCode(),lData.getDecimal_digits(),
                                 lData.getRounding(),
                                 lData.getMoneyOneDay(),lData.getMoneyOneWeek(),lData.getMoneyOneMonth(),
                                 lData.getMoneyThreeMonth(),lData.getMoneyOneYear());
             }
             logger.info("Count response for "+lData.getCountryCode()+"  count is "+count);
             if (i == filterHistoryModel.getListofCountry().size()-1)
             {
                 isLast = true;
             }
          }

          return responseUtils.constructResponse(200,commonUtils.writeAsString(objectMapper,new ApiResponse(isLast,
                  "Api Details Checked please check")));
        }
        catch (Exception exception)
        {
            logger.info("Exception in values due to "+exception.getMessage(),exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }
}
