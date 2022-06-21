package nr.king.familytracker.constant;

public class QueryConstants {
    public static final String selectNumberWithToken ="select USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE,CREATED_AT,UPDATED_AT from NUMBER_FOR_USERS where USER_ID=?";
    public static final String SELECT_USER_DETAILS_COUNT ="select count(*) from WE_TRACK_USERS where USER_ID=?";
    public static final String UPDATE_TOKEN_HEADER_IN_NUMBER_MOBILE ="update NUMBER_FOR_USERS set NUMBER=?,TOKEN_HEADER=?," +
            "COUNTRY_CODE=?,UPDATED_AT=current_timestamp where USER_ID=? and NUMBER=?";

    public static final String SELECT_USER_EXPIRY_TIME ="select Expiry_TIME,IS_USER_CREATED_IN_WETRACK_SERVICE from WE_TRACK_USERS where USER_ID=?";

    public static final String GET_ALL_MOBILE_NUMBER ="select USER_ID,NUMBER,TOKEN_HEADER,COUNTRY_CODE,NICK_NAME,PUSH_TOKEN,EXPIRY_TIME from NUMBER_FOR_USERS where USER_ID=?";

    public static final String UPDATE_TOKEN_HEADER = "update WE_TRACK_USERS  set TOKEN_HEADER=? where USER_ID=?";



}
