package com.app.utility;

public class AppLog {

    private static final boolean isDebug = true;

    public static void Log(String tag, String message) {
        if (isDebug) {

            android.util.Log.i(tag, message + "");
        }
    }

// --Commented out by Inspection START (7/27/2016 3:26 PM):
    public static void handleException(String tag, Exception e) {
       if (isDebug) {
           if (e != null) {
               android.util.Log.d(tag, e.getMessage() + "");
               e.printStackTrace();
           }
       }
   }
// --Commented out by Inspection STOP (7/27/2016 3:26 PM)

}
