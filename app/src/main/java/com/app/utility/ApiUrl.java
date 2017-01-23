package com.app.utility;

/**
 * Created by VIJAY on 01-09-2016.
 */
public class ApiUrl {

    //private static final String BASE_URL = "http://yourtracker.amazesoftsolutions.in";
    private static final String BASE_URL = "http://yourtracker.your-apps.biz"; //live
    //private static final String BASE_URL = "http://dev.yourtracker.your-apps.biz"; // dev
    private static final String SERVICE_URL = BASE_URL + "/Services/YourTracker.svc";

    public static final String LOGIN = SERVICE_URL + "/LoginR"; //{UserName}/{Password}/{DeviceToken}/{DeviceId}
    public static final String GET_SITES = SERVICE_URL + "/GetSitesR"; //{Auth}
    public static final String START_SERVICE = SERVICE_URL + "/TrackR"; //{Auth}/{Switch}/{Date}/{Site}/{Incident}/{Lat}/{Lng}
    public static final String STOP_SERVICE = SERVICE_URL + "/TrackStopR";
    public static final String SEND_LOCATION = SERVICE_URL + "/SendLocationR"; //{Auth}/{TrackId}/{Date}/{Lat}/{Lng}
    public static final String LOGOUT = SERVICE_URL + "/LogOutR"; //{Auth}

}
