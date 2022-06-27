package nr.king.familytracker.constant;

public class LocationTrackingConstants {

 public static final String STATUS = "status";
 public static final String WETRACK = "we_tracker_";
 public static final String EVENT_HISTORY = "eventHistory";
 public static final String VERSION_CODE ="3.1.3";
 public static final String APP_ID ="familyTrack";
 public static final String FALSE = "false";
 public static final String FAILED = "failed";
 public static final String MESSAGE = "message";
 public static final String EVENT_MASTER = "eventMaster";
 public static final String POST_NUMBER = "http://chattrack.apiservicessarl.com/api/user/addNumberForUser";
 public static final String CREATE_USER = "http://chattrack.apiservicessarl.com/api/user/newUser";
 public static final String GET_LAST_HISTORY = "http://chattrack.apiservicessarl.com/api/user/getLastHistories";
 public static final String GET_APP_USER = "http://chattrack.apiservicessarl.com/api/user/getUserAbuzer";
 public static final String GET_ACTIVE_MESSAGE = "http://chattrack.apiservicessarl.com/api/errormessage/GetActiveMessage";
 public static final String GET_COUNTRY_CODE = "http://wtrackonline.com/countrycodes.json";
 public static final String INIT_VIEW = "http://api.wtrackonline.com/api/user/initNew?version=3.1.3";
 public static final String SUBSCIBE_STATUS = "http://chattrack.apiservicessarl.com/api/user/setSubscriptionStatus";
 public static final String GET_HISTORY = "http://chattrack.apiservicessarl.com/api/history/GetHistoriesByDate";
 public static final String REMOVE_NUMBER = "http://api.wtrackonline.com/api/user/removeNumberForUser";


 public static final String[] SUBSCRIBTION_MODEL_ARRAYLIST = {"demo","Standard","Popular","PromoCode","Deluxe"};
 public static final int[]    MAX_NUMBER_ALLOWED = {1,10,30,4,100};

 private LocationTrackingConstants() {

 }
}
