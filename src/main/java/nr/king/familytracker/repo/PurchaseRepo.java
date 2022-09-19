package nr.king.familytracker.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.exceptions.FailedResponseException;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.ApiResponse;
import nr.king.familytracker.model.http.HttpResponse;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.purchaseModel.*;
import nr.king.familytracker.utils.CommonUtils;
import nr.king.familytracker.utils.HttpUtils;
import nr.king.familytracker.utils.ResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;
import static nr.king.familytracker.constant.QueryConstants.*;

@Repository
public class PurchaseRepo {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponseUtils responseUtils;

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    String urlsWithAppend = "to=AED&from=INR&amount=330";
    private static final Logger logger = LogManager.getLogger(PurchaseRepo.class);

    @Autowired
    private HttpUtils httpUtils;

    @Transactional
    public ResponseEntity makeOrder(PurchaseRequestModel purchaseRequestModel) {
        try {
            logger.info("purchaseRequestModel " + commonUtils.writeAsString(objectMapper, purchaseRequestModel));
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(selectNumberWithToken, purchaseRequestModel.getUserId(), purchaseRequestModel.getPackageName());
            if (sqlRowSet.next()) {
                int count = updatePuchaseDetails(purchaseRequestModel);
                if (count == 0) {
                    count = createPurchaseDetails(purchaseRequestModel);
                }
                if (count == 1) {
                    count = insertintoHistoryUPI(purchaseRequestModel);
                    new Thread(() -> updatePurchaseHistoryForUser(purchaseRequestModel)).start();
                }
                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new ApiResponse(count == 1, count == 1 ? "Payment Successfully" : "Payment " +
                        "Unsuccessfully Contact FamilyTracker If any Issue")));
            }

            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(false, "No User found")));
        } catch (Exception exception) {
            logger.error("Exception in Purchase history due to" + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    private void updatePurchaseHistoryForUser(PurchaseRequestModel purchaseRequestModel) {
        int maxNumber = 1;
        String expiryTime = "";
        for (int i = 0; i < SUBSCRIBTION_MODEL_ARRAYLIST.length; i++) {
            if (SUBSCRIBTION_MODEL_ARRAYLIST[i].equals(purchaseRequestModel.getPurchaseMode())) {
                maxNumber = MAX_NUMBER_ALLOWED[i];
                if (maxNumber==2 && i==1)
                {
                    expiryTime = LocalDateTime.now().plusHours(24).toString();
                }
               else if (maxNumber == 2 && i==2) {
                    expiryTime = LocalDateTime.now().plusDays(7).toString();
                } else if (maxNumber == 3) {
                    expiryTime = LocalDateTime.now().plusMonths(1).toString();
                } else if (maxNumber == 5) {
                    expiryTime = LocalDateTime.now().plusMonths(3).toString();
                }
               else if (maxNumber==10)
                {
                    expiryTime = LocalDateTime.now().plusYears(1).toString();
                }
                break;
            }
        }

        int count = jdbcTemplateProvider.getTemplate().update(UPDATE_PURCHASE_USER_TIME_ZONE,
                purchaseRequestModel.getPurchaseMode(),
                maxNumber,
                true,
                expiryTime,
                purchaseRequestModel.getUserId(),
                purchaseRequestModel.getPackageName()
        );

        if (count==1)
        {
            purchaseRequestModel.getHomeModel().setMobilePhone(purchaseRequestModel.getPurchaseMode());
            //need to add expiry time for user who purchasee
            purchaseRequestModel.getHomeModel().setPhoneBrand(expiryTime);
            new Thread(()->doUploadtoSchedularFunction(purchaseRequestModel.getHomeModel())).start();
        }
        logger.info("Information in count while updating user" + count);
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

    private int createPurchaseDetails(PurchaseRequestModel purchaseRequestModel) {
        return jdbcTemplateProvider.getTemplate().update(INSERT_PURCHASE_DETAILS,
                purchaseRequestModel.getUserId(), purchaseRequestModel.getPurchaseMode(),
                purchaseRequestModel.getPurcasePlatform(), purchaseRequestModel.getCountry(),
                purchaseRequestModel.getAmount(), purchaseRequestModel.getTransactionId(),
                purchaseRequestModel.getTransactionRemarks(), commonUtils.getExpiryTime(purchaseRequestModel.getPurchaseMode())
        );
    }

    private int insertintoHistoryUPI(PurchaseRequestModel purchaseRequestModel) {
        return jdbcTemplateProvider.getTemplate().update(INSERT_PURCHASE_DETAILS_HISTORY,
                purchaseRequestModel.getUserId(), purchaseRequestModel.getPurchaseMode(),
                purchaseRequestModel.getPurcasePlatform(), purchaseRequestModel.getCountry(),
                purchaseRequestModel.getAmount(), purchaseRequestModel.getTransactionId(),
                purchaseRequestModel.getTransactionRemarks(), commonUtils.getExpiryTime(purchaseRequestModel.getPurchaseMode())
        );
    }

    private int updatePuchaseDetails(PurchaseRequestModel purchaseRequestModel) {

        return jdbcTemplateProvider.getTemplate().update(UPDATE_PURCHASE_DETAILS, purchaseRequestModel.getPurchaseMode(),
                purchaseRequestModel.getPurcasePlatform(), purchaseRequestModel.getCountry(), purchaseRequestModel.getAmount(), purchaseRequestModel.getTransactionId(),
                purchaseRequestModel.getTransactionRemarks(),
                commonUtils.getExpiryTime(purchaseRequestModel.getPurchaseMode()),
                purchaseRequestModel.getUserId()
        );
    }

    public ResponseEntity getUserAPI(HomeModel homeModel) {
        try {
            SqlRowSet mobileRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(SELECT_USER_EXIT, homeModel.getId(), homeModel.getPackageName());
            if (mobileRowSet.next()) {
                SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(GET_UPI_VALUES);
                SqlRowSet countryValues = jdbcTemplateProvider.getTemplate().queryForRowSet(GET_MONEY_FOR_THAT_COUNTRY, homeModel.getCountryName().toUpperCase());
                if (countryValues.wasNull()) {
                    countryValues = jdbcTemplateProvider.getTemplate().queryForRowSet(GET_MONEY_FOR_THAT_COUNTRY, "US");
                }
                boolean hasCountryValues = countryValues.next();
                UpiTransactionValue upiTransactionValue = new UpiTransactionValue();
                ArrayList<PremiumModels> valueList = new ArrayList<>();
                while (sqlRowSet.next()) {
                    PremiumModels premiumModels = new PremiumModels();
                    if (sqlRowSet.isFirst()) {
                        upiTransactionValue.setApiId(sqlRowSet.getString("UPI_ID"));
                    }
                    premiumModels.setTopHeader(sqlRowSet.getString("PURCHASE_TYPE"));
                    premiumModels.setProductIds(sqlRowSet.getString("UPI_ID"));
                    premiumModels.setTopDescription(sqlRowSet.getString("PURCHASE_DESCRIBITION"));
                    premiumModels.setTextColor(sqlRowSet.getString("COLOR_CODE"));
                    premiumModels.setBackGroundColour(sqlRowSet.getString("COLOR_BAR"));
                    premiumModels.setPriceStag(homeModel.getCountryName().equalsIgnoreCase("us") ?
                            sqlRowSet.getString("MONEY_IN_USD") : sqlRowSet.getString("MONEY_IN_INR"));
                    if (hasCountryValues) {
                        premiumModels.setMoneyForOneDay(countryValues.getString("MONEY_ONE_DAY"));
                        premiumModels.setMoneyForOneWeek(countryValues.getString("MONEY_ONE_WEEK"));
                        premiumModels.setMoneyForOneMonth(countryValues.getString("MONEY_ONE_MONTH"));
                        premiumModels.setMoneyForThreeMonth(countryValues.getString("MONEY_THREE_MONTH"));
                        premiumModels.setMoneyForOneYear(countryValues.getString("MONEY_ONE_YEAR"));
                        premiumModels.setSymbolNative(String.valueOf(countryValues.getString("SYMBOL")));
                        premiumModels.setMoneyForThatCountry(countryValues.getString("CODE"));
                    } else {
                        premiumModels.setMoneyForOneDay("");
                        premiumModels.setMoneyForOneWeek("");
                        premiumModels.setMoneyForOneMonth("");
                        premiumModels.setMoneyForThreeMonth("");
                        premiumModels.setMoneyForOneYear("");
                        premiumModels.setSymbolNative("");
                        premiumModels.setMoneyForThatCountry("");

                    }
                    valueList.add(premiumModels);
                }
                upiTransactionValue.setValueList(valueList);

                return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new ApiResponse(true,
                        "Value of the UPI Ids are", upiTransactionValue)));

            }
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                    "No User Found")));

        } catch (Exception exception) {
            logger.error("Exception in get UPI " + exception.getMessage(), exception);
            throw new FailedResponseException("Exception in getting API" + exception.getMessage());
        }
    }

    public ResponseEntity updateTiming(HomeModel homeModel) {
        try {
            SqlRowSet sqlRowSet = jdbcTemplateProvider.getTemplate().queryForRowSet(selectNumberWithToken, homeModel.getId(), homeModel.getPackageName());
            if (sqlRowSet.next()) {
                int count = jdbcTemplateProvider.getTemplate().update(UPDATE_TIMING_USER_DATA, LocalDateTime.now().plusHours(3), homeModel.getId());

                return responseUtils.constructResponse(200, commonUtils.writeAsString(
                        objectMapper, new ApiResponse(count == 1, count == 1 ? "Timing Updated" : "Unable to Update")
                ));
            }
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper, new ApiResponse(false,
                    "No User Found")));

        } catch (Exception exception) {
            logger.error("Exception in updating Time" + exception.getMessage(), exception);
            throw new FailedResponseException(exception.getMessage());
        }
    }

    public ResponseEntity updateAppPurchase(PurchaseUpdateRequestModel premiumModelsList) {
        try {
            int count = 0;
            for (int i = 0; i < premiumModelsList.getListofUpis().size(); i++) {
                UpdateUpiDetails premiumModels = premiumModelsList.getListofUpis().get(i);
                count = jdbcTemplateProvider.getTemplate().update(UPDATE_API_PURCHASE,
                        premiumModels.getUpiId(), premiumModels.getTopHeader(), premiumModels.getTopDescription(), premiumModels.getMoneyInInr(),
                        premiumModels.getMoneyInUsd(), premiumModels.getTextColor(), premiumModels.getBackGroundColour(), premiumModels.getId());

                if (count == 0) {
                    count = jdbcTemplateProvider.getTemplate().update(ADD_API_PURCHASE,
                            premiumModels.getUpiId(), premiumModels.getTopHeader(), premiumModels.getTopDescription(), premiumModels.getMoneyInInr(),
                            premiumModels.getMoneyInUsd(), premiumModels.getTextColor(), premiumModels.getBackGroundColour());
                }
            }
            return responseUtils.constructResponse(200, commonUtils.writeAsString(objectMapper,
                    new ApiResponse(count == 1, (count == 1) ? "Upi Updated Sucessfully" : "Upi Updated Failed")));

        } catch (Exception exception) {
            logger.info("Exception in app update data" + exception.getMessage(), exception);
            throw new FailedResponseException("Unable to Update Purchase");
        }
    }
}
