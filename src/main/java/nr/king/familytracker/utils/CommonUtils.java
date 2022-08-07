package nr.king.familytracker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nr.king.familytracker.jdbc.JdbcTemplateProvider;
import nr.king.familytracker.model.http.RandomString;
import nr.king.familytracker.model.http.homeModel.HomeModel;
import nr.king.familytracker.model.http.purchaseModel.PurchaseRequestModel;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.*;
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
        headersMap.put("Accept:","application/json");
        headersMap.put("Content-Type", "application/json");
        headersMap.put("X-Auth-Token", authHeader);
        headersMap.put("User", authHeader);
        return headersMap;
    }



    public String getExpiryTime(String purchaseMode)
    {

        String expiryTime="";
        for (int i=0;i<SUBSCRIBTION_MODEL_ARRAYLIST.length;i++)
        {
            if (SUBSCRIBTION_MODEL_ARRAYLIST[i].equals(purchaseMode))
            {
              int   maxNumber =  MAX_NUMBER_ALLOWED[i];
                if (maxNumber==10)
                {
                    expiryTime = LocalDateTime.now().plusDays(7).toString();
                }
                else if (maxNumber==30)
                {
                    expiryTime = LocalDateTime.now().plusMonths(1).toString();
                }
                else if (maxNumber==100)
                {
                    expiryTime = LocalDateTime.now().plusMonths(3).toString();
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

       /* if (Integer.valueOf(outletId) > 0) {
            stringBuilder.append(isAndNeed ? String.format(" and outlet_customer_id  = '%s' ", outletId) : String.format(" outlet_customer_id  = '%s' ", outletId));
        }*/
    }


    public boolean isValidSkewCode(int skewCode) {
        return skewCode == 520 || skewCode == 538 || skewCode == 260;
    }

    public HomeModel getHomeModel(String token_header,boolean isFirstTime) {
        HomeModel homeModel = new HomeModel();
        String string = UUID.randomUUID().toString().substring(0,12).replace("-","f");
        Map<String, String[]> phoneBrandsMap = new HashMap<>();
        phoneBrandsMap.put("Samsung", new String[]{
                "Galaxy S22",
                "Galaxy A13 5G",
                "Galaxy A53 5G",
                "S21 FE 5G",
                "S22 Ultra",
                "Z Flip3 5G"
        });
        if (string.equals(token_header) && !isFirstTime) {
            getHomeModel(token_header, false);
        }

        Pair<String, String> stringPair = getRandomMap(phoneBrandsMap);
        homeModel.setId((isFirstTime)? token_header:string);
        homeModel.setMobilePhone((isFirstTime)? token_header:string);
        homeModel.setPhoneModel(stringPair.getSecond());
        homeModel.setPhoneBrand(stringPair.getFirst());
        homeModel.setOneSignalExternalUserId((isFirstTime)? token_header:string);
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
        return string == null || string.isEmpty() || "null".equalsIgnoreCase(string)?"":string;
    }
}
