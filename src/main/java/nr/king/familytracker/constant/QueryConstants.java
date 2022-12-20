package nr.king.familytracker.constant;

public class QueryConstants {
    public static final String selectNumberWithToken ="select USER_ID,NUMBER,TOKEN_HEADER,NICK_NAME,COUNTRY_CODE,CREATED_AT,UPDATED_AT from " +
            "NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?";

    public static  final String GET_NOT_DEMO_USERS ="select user_id,Expiry_TIME,PACKAGE_NAME from WE_TRACK_USERS where purchase_mode!='demo'";
    public static  final String GET_CURRENT_USER ="select user_id,Expiry_TIME,PACKAGE_NAME from WE_TRACK_USERS where USER_ID=?";

    public static final String SELECT_USER_DETAILS_COUNT ="select count(*) from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?";
    public static final String SELECT_NUMBER_DETAILS_COUNT ="select count(*) from NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?";
    public static final String UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE ="update NUMBER_FOR_USERS set NUMBER=?,TOKEN_HEADER=?," +
            "COUNTRY_CODE=?,UPDATED_AT=current_timestamp,NICK_NAME=? where USER_ID=? and NUMBER=? and PACKAGE_NAME=?";

    public  static  final  String  ISNUMBER_HAVING_USER="select USER_ID,NUMBER from NUMBER_FOR_USERS where USER_ID=? and NUMBER=? and PACKAGE_NAME=?";


    public static final String UPDATE_DETAILS_IN_NUMBER_MOBILE ="update NUMBER_FOR_USERS set NUMBER=?," +
            "COUNTRY_CODE=?,UPDATED_AT=current_timestamp,NICK_NAME=? where USER_ID=? and NUMBER=? and PACKAGE_NAME=?";

    public static final String UPDATE_HEADER_FOR_MOBILE ="update NUMBER_FOR_USERS set TOKEN_HEADER=? " +
            " where USER_ID=? and NUMBER=? and PACKAGE_NAME=?";


    public static final String SELECT_USER_EXPIRY_TIME ="select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE,purchase_mode,MAX_NUMBER,CREDIT_LIMIT,PACKAGE_NAME,CREATED_AT,UPDATED_AT from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?";

    public static final String SELECT_USER_EXPIRY_TIME_WITH_ACCOUNT_DETAILS ="select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE,MAX_NUMBER," +
            "purchase_mode,CREDIT_LIMIT,PACKAGE_NAME from WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?";

    public static final String GET_ALL_MOBILE_NUMBER ="select USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE,NICK_NAME,PUSH_TOKEN,EXPIRY_TIME,PACKAGE_NAME from NUMBER_FOR_USERS where USER_ID=? and PACKAGE_NAME=?";

    public static final String UPDATE_TOKEN_HEADER = "update WE_TRACK_USERS  set TOKEN_HEADER=? where USER_ID=? and PACKAGE_NAME=?";

    public static final String UPDATE_TIMING_USER_DATA = "update WE_TRACK_USERS  set Expiry_TIME=? where USER_ID=? and PACKAGE_NAME=?";

    public static final String UPDATE_PURCHASE_USER_TIME_ZONE = "update WE_TRACK_USERS  set purchase_mode=?,MAX_NUMBER=?,IS_PURCHASED=?,Expiry_TIME=?" +
            " where USER_ID=? and PACKAGE_NAME=?";


    public static final String UPDATE_PURCHASE_DETAILS = "UPDATE PURCHASED_DETAILS set PURCHASE_MODE=?,PURCHASE_PLATFORM=?,COUNTRY=?,AMOUNT=?,TRANSATION_ID=?,TRANSACTION_REMARK=?," +
            "EXPIRY_DATE=?,UPDATED_AT=current_timestamp where USER_ID=?";

    public static final String INSERT_PURCHASE_DETAILS = "insert into  PURCHASED_DETAILS (USER_ID,PURCHASE_MODE,PURCHASE_PLATFORM,COUNTRY,AMOUNT," +
         "TRANSATION_ID,TRANSACTION_REMARK,EXPIRY_DATE,CREATED_AT,UPDATED_AT) values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp)";

    public static final String INSERT_PURCHASE_DETAILS_HISTORY = "insert into  PURCHASED_DETAILS_HISTORY (USER_ID,PURCHASE_MODE,PURCHASE_PLATFORM,COUNTRY,AMOUNT," +
         "TRANSATION_ID,TRANSACTION_REMARK,EXPIRY_DATE,CREATED_AT,UPDATED_AT) values (?,?,?,?,?,?,?,?,current_timestamp,current_timestamp)";

    public static  final String GET_UPI_VALUES = "select UPI_ID,PURCHASE_TYPE,PURCHASE_DESCRIBITION,MONEY_IN_INR,MONEY_IN_USD,COLOR_CODE,COLOR_BAR,CREATED_AT,UPDATED_AT,button_color," +
            "button_bg,offer_price,offer_percentage " +
            "from UPI_DETAILS";

    public static  final  String UPDATE_PUSH_NOTIFICATION =
            "update NUMBER_FOR_USERS set is_noti_enabled=? where USER_ID=? and NUMBER=? and PACKAGE_NAME=?";

    public static final String UPDATE_API_PURCHASE="UPDATE UPI_DETAILS SET UPI_ID=?,PURCHASE_TYPE=?,PURCHASE_DESCRIBITION=?," +
            "MONEY_IN_INR=?,MONEY_IN_USD=?,COLOR_CODE=?,COLOR_BAR=?,UPDATED_AT=current_timestamp,button_color=?,button_bg=?," +
            "offer_price=?,offer_percentage=? WHERE ID=?";

    public static final String ADD_API_PURCHASE ="insert into UPI_DETAILS(UPI_ID,PURCHASE_TYPE,PURCHASE_DESCRIBITION,MONEY_IN_INR,MONEY_IN_USD," +
            "COLOR_CODE,COLOR_BAR,CREATED_AT,UPDATED_AT,button_color,button_bg,offer_price,offer_percentage)" +
            "values (?,?,?,?,?,?,?,current_timestamp,current_timestamp,?,?,?,?)";

    public static final  String INSERT_COUNTRY_CURRENCY =
            "insert into CURRENCY_VALUES (SYMBOL,CODE,SYMBOL_NATIVE,COUNTRY_CODE,DECIMAL_DIGITS,ROUNDING,MONEY_ONE_DAY,MONEY_ONE_WEEK,MONEY_ONE_MONTH," +
                    "MONEY_THREE_MONTH,MONEY_ONE_YEAR,CREATED_AT,UPDATED_AT) " +
             "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp,current_timestamp)";


    public static  final  String UPDATE_NUMBER_CURRENCY=
            "update CURRENCY_VALUES set SYMBOL=?,CODE=?,COUNTRY_CODE=?,DECIMAL_DIGITS=?,ROUNDING=?,MONEY_ONE_DAY=?,MONEY_ONE_WEEK=?," +
                    "MONEY_ONE_MONTH=?,MONEY_THREE_MONTH=?,MONEY_ONE_YEAR=?," +
                    "UPDATED_AT=current_timestamp where ID=?";

    public static final  String GET_MONEY_FOR_THAT_COUNTRY = "SELECT MONEY_ONE_DAY,MONEY_ONE_WEEK,MONEY_ONE_MONTH,MONEY_THREE_MONTH,MONEY_ONE_YEAR,SYMBOL,CODE  " +
            "FROM currency_values WHERE COUNTRY_CODE=?";


    public static final String SELECT_USER_EXIT ="select USER_ID from " +
            "WE_TRACK_USERS where USER_ID=? and PACKAGE_NAME=?";


    public static final String SELECT_EVENT_BASED_ON_COUNTRY =
            "SELECT EVENT_NAME,EVENT_NORMAL_IMAGE,EVENT_IMAGE,EVENT_BODY,EVENT_COUNTRY,IMAGE_MORNING,IMAGE_AFTERNOON,IMAGE_EVENING,IMAGE_NIGHT,EVENT_ID FROM EVENT_TABLE " +
                    "WHERE EVENT_COUNTRY=?";


    public  static  final String SELECT_COUNTRY_VALUE
            = "SELECT EVENT_ID,USER_ID,EVENT_COUNTRY FROM EVENT_HISTORY where EVENT_ID=? AND USER_ID=?";


    public static final String INSERT_VALUES_IN_EVENT="INSERT INTO EVENT_TABLE(EVENT_NAME,EVENT_NORMAL_IMAGE,EVENT_IMAGE,EVENT_BODY,EVENT_COUNTRY," +
            "IMAGE_MORNING,IMAGE_AFTERNOON,IMAGE_EVENING,IMAGE_NIGHT,EVENT_ID,CREATED_AT,UPDATED_AT) " +
            "values(?,?,?,?,?,?,?,?,?,?,current_timestamp,current_timestamp)";


    public  static  final String UPDATE_VALUES_IN_EVENT = "UPDATE EVENT_TABLE set EVENT_NAME=?,EVENT_NORMAL_IMAGE=?,EVENT_IMAGE=?,EVENT_BODY=?," +
            "EVENT_COUNTRY=?,IMAGE_MORNING=?,IMAGE_AFTERNOON=?,IMAGE_EVENING=?," +
            "IMAGE_NIGHT=?,EVENT_ID=?,UPDATED_AT=current_timestamp WHERE ID=?";


    public static final String UPDATE_POST_CLICK_VALUES="UPDATE EVENT_HISTORY SET EVENT_ID=?,USER_ID=?,EVENT_COUNTRY=? WHERE EVENT_ID=?";
    public static final String INSERT_POST_CLICK_VALUES="INSERT INTO EVENT_HISTORY(EVENT_ID,USER_ID,EVENT_COUNTRY) VALUES (?,?,?)";

    public static  final String UPDATE_PUBLIC_MESSAGE_VALUES="UPDATE MESSAGE_BETWEEN_USER SET MESSAGE=?,MESSAGE_IMAGE_URL=? WHERE MESSAGE_USER_ID=? AND ID=?";

    public static  final String UPDATE_LAST_SEEN_QUERY = "UPDATE MESSAGE_BETWEEN_USER SET IS_SEEN=? WHERE MESSAGE_USER_ID=? and ID=?";
    public static  final String INSERT_PUBLIC_MESSAGE_VALUES="INSERT INTO MESSAGE_BETWEEN_USER(MESSAGE,MESSAGE_USER_ID,MESSAGE_IMAGE_URL," +
            "ADMIN_ID,CREATED_AT,UPDATED_AT,IS_SEEN,MESSAGER_ID) " +
            "VALUES (?,?,?,?,current_timestamp,current_timestamp,?,?)";


    public static final String SELECT_ALL_MESSAGE = "SELECT ID,MESSAGE,MESSAGE_USER_ID,MESSAGE_IMAGE_URL,ADMIN_ID,CREATED_AT,UPDATED_AT,IS_SEEN," +
            "MESSAGER_ID FROM MESSAGE_BETWEEN_USER";

    public  static  final String  UPDATE_MESSAGE_BASED_APPLICATION =
            "UPDATE MESSAGE_BASED_APPLICATION SET IS_FREE_MODE=?,APP_VERSION=?,UPDATED_AT=?,IS_FORCE_UPDATE=? WHERE ID=?";

    public static final String SELECT_APP_FLOW =
            "SELECT IS_FREE_MODE,APP_VERSION,IS_FORCE_UPDATE FROM MESSAGE_BASED_APPLICATION";

    public static  final String  UPDATE_APP_FLOW="UPDATE MESSAGE_BASED_APPLICATION SET IS_FREE_MODE=?,APP_VERSION=?," +
            "IS_FORCE_UPDATE=?,UPDATED_AT=current_timestamp where ID=1";
    public static  final String INSERT_APP_FLOW ="INSERT INTO MESSAGE_BASED_APPLICATION(IS_FREE_MODE,APP_VERSION,IS_FORCE_UPDATE,CREATED_AT,UPDATED_AT) " +
            "VALUES (?,?,?,current_timestamp,current_timestamp)";


    public  static  final  String INSERT_INTO_AUDIT_MASTER =
            "INSERT INTO AUDIT_MASTER (USER_ID,MODULES,IP_ADDRESS,TASK,TODAY,PACKAGE_NAME,CREATED_AT,UPDATED_AT) VALUES" +
                    "(?,?,?,?,?,?,current_timestamp,current_timestamp)";


}
