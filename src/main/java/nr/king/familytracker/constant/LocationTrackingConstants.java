package nr.king.familytracker.constant;

import java.lang.reflect.Array;

public class LocationTrackingConstants {

    public static final String STATUS = "status";
    public static final String WETRACK = "we_tracker_";
    public static final String EVENT_HISTORY = "eventHistory";
    public static final String VERSION_CODE = "3.1.3";
    public static final String APP_ID = "familyTrack";
    public static final String FALSE = "false";
    public static final String FAILED = "failed";
    public static final String MESSAGE = "message";
    public static final String EVENT_MASTER = "eventMaster";
    public static final int DOLLARS_PREMIUM = 83;
    public static final int NUMBER_NEEDED = 100;
    public static final int USER_CREATED_FAILED = -101;
    public static final int USER_CREATED_FAILED_IN_LOCAL_DB = -102;
    public static final int UPDATE_USER_SUCESS = 1000;
    public static final int UPDATE_USER_FAILED = -1000;
    public static boolean IS_MONEY_MODE = false;
    public static String APP_VERSION = "1.3";
    public static boolean IS_FORCE_UPDATE = false;
    public static boolean IS_APP_INACTIVE = false;
    public static String TOPIC_ADDING = "topic/";
    public static final String EMPTY_STRING = "Welcome,to Family Tracking Application.World Trusted Application.\uD83D\uDE00";
    public static final String WILL_UPDATE_STRING = "We will update your Query to Support Team. we will get back Soon.\uD83D\uDC4D";


    //api for apiTracker
    public static final String GET_HISTORY = "http://api.wtrackonline.com/api/history/GetHistoriesByDate";
    public static final String POST_NUMBER = "http://api.wtrackonline.com/api/user/addNumberForUser";

    public static final String CREATE_USER = "http://api.wtrackonline.com/api/user/newUser";

    public static final String ENABLE_PUSH_NOTIFICATION = "http://api.wtrackonline.com/api/user/enablePush";

    public static final String GET_LAST_HISTORY = "http://api.wtrackonline.com/api/user/getLastHistories";

    public static final String GET_APP_USER = "http://api.wtrackonline.com/api/user/getUserAbuzer";


    public static final String GET_COUNTRY_CODE = "http://wtrackonline.com/countrycodes.json";

    public static final String INIT_VIEW = "http://api.wtrackonline.com/api/user/initNew?version=3.1.3";

    public static final String REMOVE_NUMBER = "http://api.wtrackonline.com/api/user/removeNumberForUser";
    public static final String QR_GENERATOR = "http://api.wtrackonline.com/api/user/Start";
    public static final String QR_GENERATOR_URL = "http://api.wtrackonline.com/api/misc/getShortURL?user=";

    public static final String FCM_PUSH = "https://fcm.googleapis.com/fcm/send";

     /*  public static final String CREATE_USER = "http://chattrack.apiservicessarl.com/api/user/newUser";
    public static final String ENABLE_PUSH_NOTIFICATION = "http://chattrack.apiservicessarl.com/api/user/enablePush";

    public static final String POST_NUMBER = "http://chattrack.apiservicessarl.com/api/user/addNumberForUser";

    public static final String SET_PUSH_NOTIFATION = "http://api.wtrackonline.com/api/user/setPushToken";

     public static final String GET_LAST_HISTORY = "http://chattrack.apiservicessarl.com/api/user/getLastHistories";

    public static final String GET_APP_USER = "http://chattrack.apiservicessarl.com/api/user/getUserAbuzer";

    public static final String GET_ACTIVE_MESSAGE = "http://chattrack.apiservicessarl.com/api/errormessage/GetActiveMessage";

    public static final String SUBSCIBE_STATUS = "http://chattrack.apiservicessarl.com/api/user/setSubscriptionStatus";

     public static final String GET_HISTORY = "http://chattrack.apiservicessarl.com/api/history/GetHistoriesByDate";*/

   /* public static final String LOCAL_HOST_TOKEN = "http://3.92.177.250:8082/we_track/v1/update-token";
    public static final String LOCAL_HOST_NUMBER = "http://3.92.177.250:8082/we_track/v1/update-status";*/

    public static final String LOCAL_HOST_NUMBER = "http://localhost:8082/we_track/v1/update-status";
    public static final String LOCAL_HOST_TOKEN = "http://localhost:8082/we_track/v1/update-token";
    public static final String LOCAL_HOST_ADD_USER = "http://localhost:8082/we_track/v1/create-deviceUser";
    public static final String[] PACKAGE_ARRAY_WITHOUT_ADD = {"com.withcodeplays.familytracker",
            "com.withcodeplays.socialmediatracker", "com.withcodeplays.familytracking"};
    public static final String[] PACKAGE_ARRAY_WITH_ADD = {
            "com.withcodeplays.wetracker",
            "com.withcodeplays.whattracker",
            "com.withcodeplays.crushtracker",
            "com.withcodeplays.onlinetracker",
            "com.withcodeplays.familytracking"};

    public static final String CURRECY_CONVERTER = "lkmiK7UKdmBwSqJmvXQtO73dEW5M6sor";


    public static final String API_FOR_CURRENCY_CONVERTION = "https://api.apilayer.com/fixer/convert?";
    public static final String[] SUBSCRIBTION_MODEL_ARRAYLIST = {"demo", "Fresher", "Standard", "Popular", "Deluxe", "Ultra Deluxe"};
    public static final int[] MAX_NUMBER_ALLOWED = {1, 2, 2, 3, 5, 10, 10};

    private LocationTrackingConstants() {

    }
}
