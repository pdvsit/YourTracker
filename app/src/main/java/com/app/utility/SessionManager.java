package com.app.utility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.app.tracker.Login;
import com.app.tracker.SelectSite;

import java.util.HashMap;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "YourTrackerPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User UID (make variable public to access from outside)
    public static final String KEY_ROLE = "role";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_STATUS = "status";
    public static final String KEY_TIME_FRAME = "timeFrame";
    public static String KEY_TRACK_ID = "trackId";
    public static String KEY_SERVICE_STATUS = "serviceStatus";

    //Save data state
    public static String KEY_INCIDENT = "incident";
    public static String KEY_SELECTED_SITE_NAME = "selectedSiteName";
    public static String KEY_SELECTED_SITE_ID = "selectedSiteId";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String role, String token, String status, String time, String trackId, String serviceStatus
    , String incident, String selectedSiteName, String selectedSiteId){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_STATUS, status);
        editor.putString(KEY_TIME_FRAME, time);
        editor.putString(KEY_TRACK_ID, trackId);
        editor.putString(KEY_SERVICE_STATUS, serviceStatus);
        editor.putString(KEY_INCIDENT, incident);
        editor.putString(KEY_SELECTED_SITE_NAME, selectedSiteName);
        editor.putString(KEY_SELECTED_SITE_ID, selectedSiteId);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, Login.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_STATUS, pref.getString(KEY_STATUS, null));
        user.put(KEY_TIME_FRAME, pref.getString(KEY_TIME_FRAME, null));
        user.put(KEY_TRACK_ID, pref.getString(KEY_TRACK_ID, null));
        user.put(KEY_SERVICE_STATUS, pref.getString(KEY_SERVICE_STATUS, null));
        user.put(KEY_INCIDENT, pref.getString(KEY_INCIDENT, null));
        user.put(KEY_SELECTED_SITE_NAME, pref.getString(KEY_SELECTED_SITE_NAME, null));
        user.put(KEY_SELECTED_SITE_ID, pref.getString(KEY_SELECTED_SITE_ID, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
