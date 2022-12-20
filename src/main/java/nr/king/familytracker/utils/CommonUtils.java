package nr.king.familytracker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.PhoneModel;
import nr.king.familytracker.model.http.UpdateAuditMasterRequestBody;
import nr.king.familytracker.model.http.dashboardModel.DashBoardRequestBody;
import nr.king.familytracker.model.http.dashboardModel.FlashSales;
import nr.king.familytracker.model.http.filterModel.FilterHistoryModel;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.messages.MessageRequestBody;
import nr.king.familytracker.model.http.purchaseModel.PurchaseRequestModel;
import nr.king.familytracker.model.http.purchaseModel.UpdateUpiDetails;
import nr.king.familytracker.repo.NotificationModel;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nr.king.familytracker.constant.LocationTrackingConstants.*;

@Component
public class CommonUtils {

    @Autowired
    private JdbcTemplateProvider jdbcTemplateProvider;

    public static final HttpStatus BAD_REQUEST = HttpStatus.valueOf(406);
    private static final Logger logger = LogManager.getLogger(CommonUtils.class);
    private static final Pattern numberMinusMinusPattern = Pattern.compile("\\d+-\\d+");
    DateTimeFormatter onlineActivityDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    String onlineStringActivity = "yyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    DateTimeFormatter onlineDateFormaterCreatedAt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);


    String[] europeanCountry = new String[]{"AUT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "GRC", "HU", "IE", "IRL", "IT", "ITA", "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE"};

    String[] northAmerica = new String[]{"CA", "MX", "MEX", "GL", "JM", "PA", "CR", "GT", "PR", "HT", "DO", "US"};
    String[] russia = new String[]{"RU", "BY"};
    String[] muslimCountry = new String[]{"AF", "DZ", "BH", "BD", "BN", "EG", "IR", "IQ", "JO", "KW", "LY", "MV", "MR", "MA", "OM", "PK", "QA", "SA", "SAU", "SO", "SOM", "TN", "AE", "ARE", "YE", "TR"};
    String[] indiaCountry = new String[]{"IN"};
    String[] london = new String[]{"GB"};
    String[] australia = new String[]{"AU"};
    String[] hinduCountry = new String[]{"NP", "MU", "LK", "BT"};
    String[] asianCountry = new String[]{"JP", "ID", "MY", "SG", "CN", "KP", "KOR", "KR", "TH", "MM"};

    public <R> R safeParseJSON(ObjectMapper objectMapper, String payload, Class<R> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Unable to parse JSON payload - %s", payload), ex);
        }
    }

    public Boolean checkSpaceOrSQLStatement(String strInput) {
        if (strInput != null && strInput.trim().length() > 0) {
            String upperStr = strInput.toUpperCase();
            logger.info("message data" + upperStr);
            return upperStr.contains("'")
                    || upperStr.contains("\"")
                    || upperStr.contains("--")
                    || upperStr.contains("CHR(")
                    || upperStr.contains(")")
                    || upperStr.contains("DBMS_PIPE.")
                    || upperStr.contains(" UNION ")
                    || upperStr.contains("SELECT ")
                    || upperStr.contains(" OR ")
                    || upperStr.contains(" AND ")
                    || (upperStr.contains("=")
                    || upperStr.contains("<")
                    || upperStr.contains(">"))
                    || numberMinusMinusPattern.matcher(strInput).matches();
        }
        return false;
    }

    public Boolean checkValidDateFormat(String date) {
        boolean valid = true;
        try {// ResolverStyle.STRICT for 30, 31 days checking, and also leap year.
            LocalDate.parse(date,
                    DateTimeFormatter.ofPattern("uuuu-M-d")
                            .withResolverStyle(ResolverStyle.STRICT)
            );
            valid = false;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            valid = true;
        }


        return valid;
    }

    public Boolean checkSpaceOrSQLStatementForUrl(String strInput) {
        if (strInput != null && strInput.trim().length() > 0) {
            String upperStr = strInput.toUpperCase();

            return (upperStr.contains("SELECT") && upperStr.contains("="))
                    || upperStr.contains("\"")
                    || upperStr.contains("--")
                    || upperStr.contains("CHR(")
                    || upperStr.contains(")")
                    || upperStr.contains("DBMS_PIPE.")
                    || upperStr.contains(" UNION ")
                    || upperStr.contains("SELECT ")
                    || upperStr.contains(" OR ")
                    || upperStr.contains(" AND ")
                    || upperStr.contains("<")
                    || upperStr.contains(">")
                    || numberMinusMinusPattern.matcher(strInput).matches();

        }
        return false;
    }


    public String getCurrentDateTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        return simpleDateFormat.format(date);
    }


    public String writeAsString(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(String.format("Unable to write value to JSON - %s", ex.getMessage()), ex);
        }
    }


    public Boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty() || "null".equalsIgnoreCase(string);
    }

    public Boolean securityCheck(String parameter) {
        return isNullOrEmpty(parameter) || checkSpaceOrSQLStatement(parameter);
    }

    public Boolean isValidEmailFormat(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }


    public byte[] writeAsBytes(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(String.format("Unable to write value to JSON - %s", ex.getMessage()), ex);
        }
    }

    public String readFromHeader(ServletRequest request, String key) {
        return ((HttpServletRequest) request).getHeader(key);
    }

    public Long getTimeDifference(String start, String end) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            return format.parse(end).getTime() - format.parse(start).getTime();
        } catch (ParseException ex) {
            throw new RuntimeException(String.format("Unable to parse value to date - %s", ex.getMessage()), ex);
        }
    }

    public Long getTimeDifferences(String start, String end) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            return format.parse(end).getTime() - format.parse(start).getTime();
        } catch (ParseException ex) {
            return 0L;
        }
    }


    public <T> T safeGetFirst(Iterator<T> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }

    public static final String jpegEndFormat = "data:image/jpeg;base64";
    public static final String jpgEndFormat = "data:image/jpg;base64";
    public static final String pngEndFormat = "data:image/png;base64";

    public String getEndDateTime(String end) {
        if (end.equalsIgnoreCase("0")) {
            logger.info("end time is - " + end);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            sd.setTimeZone(TimeZone.getTimeZone("IST"));
            return sd.format(date);
        } else {
            return end;
        }
    }

    public String base64Decode(String authToken) {
        return new String(new Base64().decode(authToken));
    }

    public String base64Encode(String input) {
        return new String(new Base64().encode(input.getBytes()));
    }

    public Map<String, String> getHeadersMap(String authHeader) {
        Map<String, String> headersMap = new LinkedHashMap<>();
        headersMap.put("Content-Type", "application/json");
        headersMap.put("X-Auth-Token", authHeader);
        headersMap.put("User", authHeader);
        return headersMap;
    }

    public Map<String, String> getHeadersMapForSpecific(String authHeader) {
        Map<String, String> headersMap = new LinkedHashMap<>();
        headersMap.put("Accept:", "application/json");
        headersMap.put("Content-Type", "application/json");
        headersMap.put("X-Auth-Token", authHeader);
        headersMap.put("User", authHeader);
        return headersMap;
    }


    public String getMaxNumber(String purchaseMode)
    {
        int maxNumber = 0;
        for (int i = 0; i < SUBSCRIBTION_MODEL_ARRAYLIST.length; i++) {
            if (SUBSCRIBTION_MODEL_ARRAYLIST[i].equals(purchaseMode)) {
                maxNumber = MAX_NUMBER_ALLOWED[i];
                break;
            }
        }
        return String.valueOf(maxNumber);
    }

    public  String maxTime(String purchaseMode)
    {
        String expiryTime = "";
        int maxNumber = 0;
        for (int i = 0; i < SUBSCRIBTION_MODEL_ARRAYLIST.length; i++) {
            if (SUBSCRIBTION_MODEL_ARRAYLIST[i].equals(purchaseMode)) {
                maxNumber = MAX_NUMBER_ALLOWED[i];
                if (maxNumber == 2 && i == 1) {
                    expiryTime = "2 days";
                } else if (maxNumber == 2 && i == 2) {
                    expiryTime = "7 days";
                } else if (maxNumber == 3) {
                    expiryTime = "1 mon";
                } else if (maxNumber == 5) {
                    expiryTime = "3 mon";
                } else if (maxNumber == 10) {
                    expiryTime = "1 year";
                } else {
                    expiryTime = "2 days";
                }
                break;
            }
        }

        return expiryTime;
    }


    public String getExpiryTime(String purchaseMode) {
        String expiryTime = "";
        int maxNumber = 0;
        for (int i = 0; i < SUBSCRIBTION_MODEL_ARRAYLIST.length; i++) {
            if (SUBSCRIBTION_MODEL_ARRAYLIST[i].equals(purchaseMode)) {
                maxNumber = MAX_NUMBER_ALLOWED[i];
                if (maxNumber == 2 && i == 1) {
                    expiryTime = LocalDateTime.now().plusHours(48).toString();
                } else if (maxNumber == 2 && i == 2) {
                    expiryTime = LocalDateTime.now().plusDays(7).toString();
                } else if (maxNumber == 3) {
                    expiryTime = LocalDateTime.now().plusMonths(1).toString();
                } else if (maxNumber == 5) {
                    expiryTime = LocalDateTime.now().plusMonths(3).toString();
                } else if (maxNumber == 10) {
                    expiryTime = LocalDateTime.now().plusYears(1).toString();
                } else {
                    expiryTime = LocalDateTime.now().plusHours(48).toString();
                }
                break;
            }
        }

        return expiryTime;
    }

    public boolean isNotNumeric(String userId) {
        boolean isNotNumeric = false;

        for (int i = 0; i < userId.length(); i++) {
            if (!Character.isDigit(i)) {
                isNotNumeric = true;
                break;
            }
        }


        return isNotNumeric;
    }

    public Map<String, String> getSellQuickHeader(String apiKey) {
        Map<String, String> headersMap = new LinkedHashMap<>();
        headersMap.put("Content-Type", "application/json");
        headersMap.put("X-Auth-Token", apiKey);
        return headersMap;
    }

    public Map<String, Object> setPagination(String query, Map<String, Object> reponse, int pageIndex, int pageSize) {
        try {
            Integer count = jdbcTemplateProvider.getTemplate().queryForObject(query, Integer.class);
            count = count != null ? count : 0;
            reponse.put("count", count <= (pageSize * pageIndex) ? (count - (pageSize * (pageIndex - 1))) < 0 ? 0 : (count - (pageSize * (pageIndex - 1))) : pageSize);
            reponse.put("totalRecords", count);
            reponse.put("currentPage", pageIndex);
            reponse.put("perPage", pageSize);
            reponse.put("totalPages", count / pageSize + 1);
            return reponse;
        } catch (Exception e) {
            logger.error("Exception while seting pagination due to - " + e.getCause(), e);
        }
        return reponse;
    }

    public String getCountWhere(long integrationAccountId, String outletId, String filter, String orFilters, int pageIndex, int pageSize) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getConditions(integrationAccountId, outletId, filter, orFilters));
        return stringBuilder.toString();
    }

    public boolean validate(List<String> stringList) {
        return stringList.stream().anyMatch(str -> str.contains("<") || str.contains(">"));
    }

    private String getConditions(long integrationAccountId, String outletId, String filter, String orFilters) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" where ");
        getBaseCondition(integrationAccountId, outletId, stringBuilder);
        boolean isFirst = true;
        if (!filter.equalsIgnoreCase("")) {
            if (!(stringBuilder.toString().endsWith("where "))) {
                stringBuilder.append(" and ");
            }
            stringBuilder.append("  ( ");
            List<String> filters = Arrays.asList(filter.split(","));
            for (String s : filters) {
                if (isFirst) isFirst = false;
                else stringBuilder.append(" and ");
                stringBuilder.append(camelCaseConverter(s));
            }
            stringBuilder.append(" )");
        }
        if (!orFilters.equalsIgnoreCase("")) {
            if (!(stringBuilder.toString().endsWith("where "))) {
                stringBuilder.append(" and ");
            }
            stringBuilder.append(getOrCondition(orFilters));
        }
        return stringBuilder.toString().endsWith("where ") ? "" : stringBuilder.toString();
    }

    private String getOrCondition(String filter) {
        boolean isFirst = true;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ( ");
        List<String> filters = Arrays.asList(filter.split(","));
        for (String s : filters) {
            if (isFirst) isFirst = false;
            else stringBuilder.append(" or ");
            stringBuilder.append(camelCaseConverter(s));
        }
        stringBuilder.append(" )");
        return stringBuilder.toString();
    }


    public String camelCaseConverter(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean specialCharacter = false;
        boolean like = false;
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
        for (char character : text.toCharArray()) {
            if (!pattern.matcher(Character.toString(character)).matches() && !specialCharacter) {
                specialCharacter = true;
                if (":".equalsIgnoreCase(Character.toString(character))) {
                    like = true;
                    stringBuilder.append(character).append(":text ilike '%");
                } else
                    stringBuilder.append(character).append(" '");
                continue;
            }
            if (Character.isUpperCase(character) && !specialCharacter) {
                stringBuilder.append("_").append(character);
            } else {
                stringBuilder.append(character);
            }
        }
        if (like) {
            stringBuilder.append("%'");
        } else {
            stringBuilder.append("'");
        }
        return stringBuilder.toString();
    }


    public Map<String, String> getHeadersMaps(String authHeader) {
        Map<String, String> headersMap = new LinkedHashMap<>();
        headersMap.put("Content-Type", "application/json");
        headersMap.put("X-Auth-Token", authHeader);
        headersMap.put("User", authHeader);
        return headersMap;
    }

    private void getBaseCondition(long integrationAccountId, String outletId, StringBuilder stringBuilder) {
        boolean isAndNeed = false;
        if (integrationAccountId != 0) {
            stringBuilder.append(String.format(" user_id = %s ", integrationAccountId));
            isAndNeed = true;
        }
    }


    public boolean isValidSkewCode(int skewCode) {
        return skewCode == 520 || skewCode == 538 || skewCode == 260;
    }

    public String getRandomString() {
        return UUID.randomUUID().toString().substring(0, 12).replace("-", "f");
    }

    public HomeModel getHomeModel(String token_header, boolean isFirstTime) {
        HomeModel homeModel = new HomeModel();
        String string = UUID.randomUUID().toString().substring(0, 12).replace("-", "f");
        Map<String, String[]> phoneBrandsMap = new HashMap<>();
        phoneBrandsMap.put("Vivo", new String[]{
                "Vivo Y21",
                "Vivo V5",
                "Vivo v20",
                "Vivo FE 5G",
                "Vivo Ultra",
                "Z Flip3 4G"
        });
        if (string.equals(token_header) && !isFirstTime) {
            getHomeModel(token_header, false);
        }

        Pair<String, String> stringPair = getRandomMap(phoneBrandsMap);
        homeModel.setId((isFirstTime) ? token_header : string);
        homeModel.setMobilePhone((isFirstTime) ? token_header : string);
        homeModel.setPhoneModel(stringPair.getSecond());
        homeModel.setPhoneBrand(stringPair.getFirst());
        homeModel.setOneSignalExternalUserId((isFirstTime) ? token_header : string);
        homeModel.setVersion(VERSION_CODE);
        homeModel.setAppId(APP_ID);
        return homeModel;

    }

    private Pair<String, String> getRandomMap(Map<String, String[]> phoneBrandsMap) {
        List<String> keysAsArray = new ArrayList<String>(phoneBrandsMap.keySet());
        Random ran = new Random();
        //int random = ran.nextInt(1);
        String keyValue = keysAsArray.get(0);
        List<String> keyBrands = new ArrayList<>(List.of(phoneBrandsMap.get(keyValue)));
        // random = ran.nextInt(0, keyBrands.size());
        return Pair.of(keyValue, keyBrands.get(2));
    }


    public String isNullOrEmty(String string) {
        return string == null || string.isEmpty() || "null".equalsIgnoreCase(string) ? "" : string;
    }


    public boolean isNullOrEmtys(String string) {
        return string == null || string.isEmpty() || "null".equalsIgnoreCase(string);
    }

    public static boolean validation(List<String> inputValueList) {
        return inputValueList.stream().anyMatch(input -> (input.contains("<") || input.contains(">")));
    }

    public boolean checkAddOrWithoutAdd(String expiry_timr, String packageName, int credit_limit) {
        return (
                (System.currentTimeMillis() <= LocalDateTime.parse(expiry_timr)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli() && Arrays.asList(PACKAGE_ARRAY_WITHOUT_ADD).contains(packageName))
                        ||
                        40 <= credit_limit && Arrays.asList(PACKAGE_ARRAY_WITH_ADD).contains(packageName)
        );
    }


    public long returnAccountRunningTime(String created_at) {
        return System.currentTimeMillis() - LocalDateTime.parse(created_at, onlineDateFormaterCreatedAt)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public long getTimeValue(String dateValue) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            return dateFormat.parse(dateValue).getTime();
        } catch (Exception exception) {
            logger.error(String.format("Exception while get TimeValue the time "));
        }
        return 0L;
    }

    public long checkTimeDifference(String dateValue) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            timeFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            Date date1 = Timestamp.valueOf(dateValue);
            Date date2 = dateFormat.parse(dateFormat.format(new Date()));
            long difference = date2.getTime() - date1.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(difference);
        } catch (Exception e) {
            logger.error(String.format("Exception while checking the time "));
        }
        return 0L;
    }


    public boolean checkDashBoardRequestModel(DashBoardRequestBody dashBoardRequestBody) {
        return checkHomeModelSecurityCheck(dashBoardRequestBody.getHomeModel()) &&
                validate(Arrays.asList(dashBoardRequestBody.getFromDate(),
                        dashBoardRequestBody.getToDate(),
                        dashBoardRequestBody.getNumber()));
    }

    public boolean checkHomeModelSecurityCheck(HomeModel homeModel) {
        return validate(Arrays.asList(isNullOrEmty(homeModel.getId()),
                isNullOrEmty(homeModel.getPackageName()),
                isNullOrEmty(homeModel.getCountryName()),
                isNullOrEmty(homeModel.getAppId()),
                isNullOrEmty(homeModel.getPhoneModel()), isNullOrEmty(homeModel.getMobilePhone()), isNullOrEmty(homeModel.getIpAddress()),
                isNullOrEmty(homeModel.getOneSignalExternalUserId()), isNullOrEmty(homeModel.getPhoneBrand()), isNullOrEmty(homeModel.getVersion()))
        );
    }

    public boolean checkPhoneModelSecurityCheck(PhoneModel phoneModel) {
        return validate(Arrays.asList(
                isNullOrEmty(phoneModel.getId()),
                isNullOrEmty(phoneModel.getPhoneNumber()), isNullOrEmty(phoneModel.getPackageName()), isNullOrEmty(phoneModel.getNickName()),
                isNullOrEmty(phoneModel.getPushToken()), isNullOrEmty(phoneModel.getCountryCode())));
    }


    public boolean checkFilterHistoryModel(FilterHistoryModel filterHistoryModel) {
        return validate(Arrays.asList(isNullOrEmty(filterHistoryModel.getSecoundNumber()), isNullOrEmty(filterHistoryModel.getStartDate()),
                isNullOrEmty(filterHistoryModel.getEndDate()), isNullOrEmty(filterHistoryModel.getPhoneNumber())));
    }

    public boolean checkNotificationModelSecurity(NotificationModel notificationModel) {
        return validate(Arrays.asList(isNullOrEmty(notificationModel.getUserId()), isNullOrEmty(notificationModel.getPushToken()),
                isNullOrEmty(notificationModel.getNickName())));
    }

    public boolean checkPurchaseRequestModelSecurity(PurchaseRequestModel purchaseRequestModel) {
        return validate(Arrays.asList(isNullOrEmty(purchaseRequestModel.getAmount()),
                isNullOrEmty(purchaseRequestModel.getPurchaseMode()), isNullOrEmty(purchaseRequestModel.getCountry()),
                isNullOrEmty(purchaseRequestModel.getCountry()), isNullOrEmty(purchaseRequestModel.getPackageName()),
                isNullOrEmty(purchaseRequestModel.getExpiryAt()), isNullOrEmty(purchaseRequestModel.getPurcasePlatform()),
                isNullOrEmty(purchaseRequestModel.getTransactionId()), isNullOrEmty(purchaseRequestModel.getTransactionRemarks()),
                isNullOrEmty(purchaseRequestModel.getAmount()), isNullOrEmty(purchaseRequestModel.getCreatedAt()),
                isNullOrEmty(purchaseRequestModel.getUserId()), isNullOrEmty(purchaseRequestModel.getExpiryDate())
        ));
    }

    public String getModel(String packageName) {
        if (Arrays.asList(PACKAGE_ARRAY_WITHOUT_ADD).contains(packageName)) {
            return "demo";
        }
        return "Add";
    }


    public Long getTimeDuration(String dateOne, String dateTwo) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(onlineStringActivity, Locale.ENGLISH);
            //dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            Date date1 = dateFormat.parse(dateOne);
            Date date2 = dateFormat.parse(dateTwo);
            long difference = date1.getTime() - date2.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(difference);
        } catch (Exception e) {
            logger.error("Exception while checking the time ", e);
        }
        return 0l;
    }

    public boolean checkPremiumModel(UpdateUpiDetails premiumModels) {
        return validate(Arrays.asList(isNullOrEmty(premiumModels.getPriceStag()), isNullOrEmty(premiumModels.getTextColor()),
                isNullOrEmty(premiumModels.getTopDescription()), isNullOrEmty(premiumModels.getTopHeader()),
                isNullOrEmty(premiumModels.getBackGroundColour()), isNullOrEmty(premiumModels.getMoneyInInr()), isNullOrEmty(premiumModels.getMoneyInUsd()),
                isNullOrEmty(premiumModels.getOfferPrice()), isNullOrEmty(premiumModels.getOfferPercentage()),
                isNullOrEmty(premiumModels.getButtonColor()), isNullOrEmty(premiumModels.getButtonBackGround())));
    }

    public String checkCountryState(String countryName) {
        String countryValue = "";
        countryName = countryName.toUpperCase();
        if (Arrays.asList(europeanCountry).contains(countryName)) {
            countryValue = "euro";
        } else if (Arrays.asList(muslimCountry).contains(countryName)) {
            countryValue = "mus";
        } else if (Arrays.asList(hinduCountry).contains(countryName)) {
            countryValue = "hin";
        } else if (Arrays.asList(northAmerica).contains(countryName)) {
            countryValue = "norAm";
        } else if (Arrays.asList(indiaCountry).contains(countryName)) {
            countryValue = "ind";
        } else if (Arrays.asList(asianCountry).contains(countryName)) {
            countryValue = "asian";
        } else if (Arrays.asList(russia).contains(countryName)) {
            countryValue = "rus";
        } else if (Arrays.asList(london).contains(countryName)) {
            countryValue = "uk";
        } else if (Arrays.asList(australia).contains(countryName)) {
            countryValue = "aus";
        } else {
            countryValue = "common";
        }
        return countryValue;
    }


    public boolean checkFlashSalesSecurityCheck(FlashSales flashSales) {
        return validate(Arrays.asList(isNullOrEmty(flashSales.getFlashTitle()), isNullOrEmty(flashSales.getFlashBody()),
                isNullOrEmty(flashSales.getMornigImageUrl()), isNullOrEmty(flashSales.getAfternoonImageUrl()), isNullOrEmty(flashSales.getEveningImageUrl()),
                isNullOrEmty(flashSales.getNightImageUrl()), isNullOrEmty(flashSales.getCountryName()), isNullOrEmty(flashSales.getFlashImageUrl()),
                isNullOrEmty(flashSales.getEventImageUrl()), isNullOrEmty(flashSales.getEventId())
        ));
    }

    public boolean checkMessageRequestBodySecurityCheck(MessageRequestBody flashSales) {
        return checkHomeModelSecurityCheck(flashSales.getHomeModel()) || validate(Arrays.asList(isNullOrEmty(flashSales.getMessageReponseBody().getMessageImageUrl()),
                isNullOrEmty(flashSales.getMessageReponseBody().getMessage()), isNullOrEmty(flashSales.getMessageReponseBody().getMessageUserId())));
    }

    public boolean checkUpdateAuditMasterRequestBody(UpdateAuditMasterRequestBody lData) {
        return checkHomeModelSecurityCheck(lData.getHomeModel()) ||
                validate(Arrays.asList(isNullOrEmty(lData.getTask()),
                isNullOrEmty(lData.getToday()), isNullOrEmty(lData.getModules()))
        );
    }
}
