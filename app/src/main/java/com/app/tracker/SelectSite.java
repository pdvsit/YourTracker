package com.app.tracker;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.adaptor.SpinnerAdaptor;
import com.app.interfaces.WebServiceInterface;
import com.app.model.SiteModel;
import com.app.servicehandler.HttpService;
import com.app.utility.ApiUrl;
import com.app.utility.AppLog;
import com.app.utility.Connectivity;
import com.app.utility.Constants;
import com.app.utility.JsonParseKey;
import com.app.utility.ServiceCode;
import com.app.utility.SessionManager;
import com.app.utility.Singleton;
import com.app.utility.Validation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SelectSite extends AppCompatActivity implements WebServiceInterface,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText comment_edt;
    private Button select_site_btn;
    private CoordinatorLayout coordinatorLayout, snackbarlocation;
    private Spinner spinner_site;
    private ProgressBar loader;

    private SpinnerAdaptor spinnerAdaptor;
    private Context mContext;
    private SessionManager sessionManager;
    private HashMap<String, String> yourTrackerUserDetails;

    private ArrayList<SiteModel> siteList = new ArrayList<SiteModel>();

    private Button start_btn, stop_btn;

    private double lat = 0, lon = 0;
    private int getTimeFrame ;
    private String _getTimeFrame = null;

    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private GoogleApiClient googleApiClient;
    Handler mHandler = new Handler();

    private String getTrackID = null;
    //private String getSelectedSiteName = null;
    //private String getSelectedSiteId = null;
    private String getSelectedIncident = null;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_site);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        initWidget();
        loadWebsites();

        String serviceStatus = yourTrackerUserDetails.get(SessionManager.KEY_SERVICE_STATUS);

        if(!Validation.isNullOrEmpty(getSelectedIncident)) {
            comment_edt.setText(getSelectedIncident);
        } else {
            comment_edt.setText("");
        }

        AppLog.Log("serviceStatus: ", serviceStatus +"");

        if(serviceStatus != null && serviceStatus.equalsIgnoreCase("Yes")) {
            startRepeatingTask();
            start_btn.setEnabled(false);
            start_btn.getBackground().setAlpha(100);

            stop_btn.setEnabled(true);
            stop_btn.getBackground().setAlpha(200);

        } else {
            stopRepeatingTask();
            start_btn.setEnabled(true);
            start_btn.getBackground().setAlpha(200);

            stop_btn.setEnabled(false);
            stop_btn.getBackground().setAlpha(100);
        }



    }



    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {

            AppLog.Log("getTimeFrame: ", "Min: "+_getTimeFrame+ "TimeMil: "+getTimeFrame +"");
            mHandler.postDelayed(mHandlerTask, getTimeFrame);
            String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
            String trackId =  yourTrackerUserDetails.get(SessionManager.KEY_TRACK_ID);
            if(getTrackID == null) {
                getTrackID = trackId;
            }
            String getDate = getDateTime();

            if(Validation.isNullOrEmpty(getToken)) {
                sessionManager.logoutUser();
                Singleton.getInstance(mContext).ShowSnackMessage("Session expired, please login again", coordinatorLayout, R.color.button_color);
                return;
            }

            if(Validation.isNullOrEmpty(getTrackID)) {

                Singleton.getInstance(mContext).ShowSnackMessage("Please try again", coordinatorLayout, R.color.button_color);
                return;
            }

            if(lat == 0 || lon == 0) {
                //Singleton.getInstance(mContext).ShowToastMessage("Please enable location from device ", mContext);
                showSettingsAlert();
                return;
            }

            String compUrl = ApiUrl.SEND_LOCATION +"/"+ getToken +"/"+ getTrackID +"/"+ getDate +"/"+ lat +"/"+ lon;
            launchSendPeriodicallyTask(compUrl, ServiceCode.SEND_LOCATION);

        }
    };

    void startRepeatingTask()
    {
        if(mHandlerTask != null) {
            mHandlerTask.run();
        }
    }

    void stopRepeatingTask()
    {
        if(mHandlerTask != null) {
            mHandler.removeCallbacks(mHandlerTask);
        }
    }


    private void initWidget() {
        mContext = SelectSite.this;
        sessionManager = new SessionManager(mContext);
        yourTrackerUserDetails = sessionManager.getUserDetails();
        _getTimeFrame = yourTrackerUserDetails.get(SessionManager.KEY_TIME_FRAME);
        getSelectedIncident = yourTrackerUserDetails.get(SessionManager.KEY_INCIDENT);
        Singleton.getInstance(mContext).getSelectedSiteName = yourTrackerUserDetails.get(SessionManager.KEY_SELECTED_SITE_NAME);
        Singleton.getInstance(mContext).selectedSiteId = yourTrackerUserDetails.get(SessionManager.KEY_SELECTED_SITE_ID);
        if(!Validation.isNullOrEmpty(_getTimeFrame)) {
            getTimeFrame = Integer.valueOf(_getTimeFrame); //1000 * 60 * 2;
            getTimeFrame = 1000 * 60 * getTimeFrame;
            AppLog.Log("getTimeFrame: ", "Min: "+_getTimeFrame+ "TimeMil: "+getTimeFrame +"");
        }



        comment_edt = (EditText) this.findViewById(R.id.comment_edt);
        select_site_btn = (Button) this.findViewById(R.id.select_site_btn);
        start_btn = (Button) this.findViewById(R.id.start_btn);
        stop_btn = (Button) this.findViewById(R.id.stop_btn);
        coordinatorLayout = (CoordinatorLayout) this.findViewById(R.id.coordinatorLayout);
        snackbarlocation = (CoordinatorLayout) this.findViewById(R.id.snackbarlocation);
        spinner_site = (Spinner) this.findViewById(R.id.spinner_site);
        loader = (ProgressBar) this.findViewById(R.id.loader);
        start_btn = (Button) this.findViewById(R.id.start_btn);
        stop_btn = (Button) this.findViewById(R.id.stop_btn);


        spinner_site.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                // Here you get the current item (a User object) that is selected by its position
                SiteModel sites = spinnerAdaptor.getItem(position);

                AppLog.Log("Selected: ", sites.getId() +" "+sites.getName());
                Singleton.getInstance(mContext).selectedSiteId = sites.getId();
                if(Singleton.getInstance(mContext).getSelectedSiteName == null) {
                    if (!Validation.isNullOrEmpty(sites.getName()) && (sites.getName().equalsIgnoreCase(getResources().getString(R.string.lbl_select_site)))) {
                        select_site_btn.setText(getResources().getString(R.string.lbl_select_site));
                    } else {
                        select_site_btn.setText(sites.getName());
                        Singleton.getInstance(mContext).selectedSiteId = sites.getId();
                        Singleton.getInstance(mContext).getSelectedSiteName = sites.getName();
                    }
                } else {
                    select_site_btn.setText(Singleton.getInstance(mContext).getSelectedSiteName);
                }
                Singleton.getInstance(mContext).getSelectedSiteName = null;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
                AppLog.Log("Nothing: ", "Selected");
                if (!Validation.isNullOrEmpty(Singleton.getInstance(mContext).getSelectedSiteName)) {
                    select_site_btn.setText(Singleton.getInstance(mContext).getSelectedSiteName);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Setting Dialog Title
        alertDialog.setTitle(R.string.GPSAlertDialogTitle);

        //Setting Dialog Message
        alertDialog.setMessage(R.string.GPSAlertDialogMessage);

        //On Pressing Setting button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
                if(mContext != null) {

                }
            }
        });

        alertDialog.show();
    }

    private void loadWebsites() {
        if (!Connectivity.isConnected(mContext)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_error_network), coordinatorLayout, R.color.button_color);
            return;
        }
        loader.setVisibility(View.VISIBLE);

        String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
        String compUrl = ApiUrl.GET_SITES + "/" + getToken;
        launchLoadSiteTask(compUrl, ServiceCode.GET_SITES);
    }

    public void myStartServiceButton(View view) {
        String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
        String getIncident = comment_edt.getText().toString();
        String selectedSiteId = yourTrackerUserDetails.get(SessionManager.KEY_SELECTED_SITE_ID);

        String getSelectedSite = select_site_btn.getText().toString();
        String getDate = getDateTime();

        if(getSelectedSite != null && getSelectedSite.equalsIgnoreCase(getResources().getString(R.string.lbl_select_site))) {
            Singleton.getInstance(mContext).ShowSnackMessage("Please select site", coordinatorLayout, R.color.button_color);

            return;
        }

        if(selectedSiteId == null || Integer.valueOf(selectedSiteId) == 0) {
            selectedSiteId = Singleton.getInstance(mContext).selectedSiteId;
        }

        AppLog.Log("Selected_site: ", selectedSiteId);
        if(Validation.isNullOrEmpty(selectedSiteId) ||
                selectedSiteId.equalsIgnoreCase("0")) {
            Singleton.getInstance(mContext).ShowSnackMessage("Please select site", coordinatorLayout, R.color.button_color);
            return;
        }
        if(Validation.isNullOrEmpty(getIncident)) {
            Singleton.getInstance(mContext).ShowSnackMessage("Please enter incident", coordinatorLayout, R.color.button_color);
            return;
        }

        if(lat == 0 || lon == 0) {
            //Singleton.getInstance(mContext).ShowToastMessage("Please enable location from device ", mContext);
            showSettingsAlert();
            return;
        }
        String compUrl = ApiUrl.START_SERVICE +"/"+ getToken +"/"+ Constants.START_SWITCH +"/"+
                getDate.replaceAll(" ", "%20") +"/"+
                selectedSiteId +"/"+
                getIncident.replaceAll(" ", "%20") +"/"+ lat +"/" + lon;
        launchStartStopTask(compUrl, ServiceCode.START_BUTTON);
    }

    public void mySelectSite(View view) {
        spinner_site.performClick();
    }

    public void myStopServiceButton(View view) {

        String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
        String trackId = yourTrackerUserDetails.get(SessionManager.KEY_TRACK_ID);
        //String getIncident = comment_edt.getText().toString();
        String getDate = getDateTime();

        if(trackId == null) {
            trackId = getTrackID;
        }
      /*  if(Validation.isNullOrEmpty(getIncident)) {
            Singleton.getInstance(mContext).ShowToastMessage("Please enter incident", mContext);
            return;
        }*/

        if(lat == 0 || lon == 0) {
            //Singleton.getInstance(mContext).ShowToastMessage("Please enable location from device ", mContext);
            showSettingsAlert();
            return;
        }
        String compUrl = ApiUrl.STOP_SERVICE +"/"+ getToken +"/"+
                trackId +"/"+
                getDate.replaceAll(" ", "%20") +
                "/"+ lat +"/" + lon;
        launchStartStopTask(compUrl, ServiceCode.STOP_BUTTON);
    }

    public void appLogout(View view) {
        String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
        String compUrl = ApiUrl.LOGOUT +"/"+  getToken;
        launchLogoutTask(compUrl, ServiceCode.LOGOUT);
    }

    @Override
    public void requestCompleted(Object obj, int serviceCode) {
        if (obj != null) {
            String getResponse = obj.toString();
            if (serviceCode == ServiceCode.GET_SITES) {
                AppLog.Log("GET_SITES", getResponse);
                loader.setVisibility(View.GONE);
                try {
                    JSONObject jsonObject = new JSONObject(getResponse);
                    JSONObject getSiteJsonObj = jsonObject.getJSONObject(JsonParseKey.SITES_RESULT);
                    int status = getSiteJsonObj.getInt(JsonParseKey.STATUS);
                    String message = getSiteJsonObj.getString(JsonParseKey.MESSAGE);
                    if (status == 1) {
                        siteList.add(new SiteModel("0", "SELECT SITE"));
                        JSONArray jsonArray = getSiteJsonObj.getJSONArray(JsonParseKey.RESPONSE);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject newJsonObj = jsonArray.getJSONObject(i);
                            String getName = newJsonObj.getString(JsonParseKey.NAME);
                            String id = newJsonObj.getString(JsonParseKey.ID);

                            siteList.add(new SiteModel(id, getName));
                        }
                        spinnerAdaptor = new SpinnerAdaptor(SelectSite.this,
                                android.R.layout.simple_spinner_item,
                                siteList);
                        spinner_site.setAdapter(spinnerAdaptor);

                    } else {
                        Singleton.getInstance(mContext).ShowSnackMessage(message, coordinatorLayout, R.color.button_color);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (serviceCode == ServiceCode.START_BUTTON) {
                AppLog.Log("START_BUTTON", getResponse);

                try {
                    JSONObject jsonObject = new JSONObject(getResponse);
                    JSONObject newJsonObj = jsonObject.getJSONObject(JsonParseKey.TRACK_RESULT);
                    String getMessage = newJsonObj.getString(JsonParseKey.MESSAGE);

                    int status = newJsonObj.getInt(JsonParseKey.STATUS);
                    if(status == 0) {
                        Singleton.getInstance(mContext).ShowSnackMessage(getMessage, coordinatorLayout, R.color.button_color);
                    } else {
                        JSONObject resObj = newJsonObj.getJSONObject(JsonParseKey.RESPONSE);
                        String _trackId = resObj.getString(JsonParseKey.ID);
                        String getSelectedSite = select_site_btn.getText().toString();
                        String getInc = comment_edt.getText().toString();
                        sessionManager.createLoginSession(yourTrackerUserDetails.get(SessionManager.KEY_ROLE),
                                yourTrackerUserDetails.get(SessionManager.KEY_TOKEN), yourTrackerUserDetails.get(SessionManager.KEY_STATUS),
                                yourTrackerUserDetails.get(SessionManager.KEY_TIME_FRAME), _trackId, "Yes", getInc,
                                getSelectedSite, Singleton.getInstance(mContext).selectedSiteId);
                        getTrackID = _trackId;

                        Singleton.getInstance(mContext).ShowSnackMessage("Your service has been started", coordinatorLayout, R.color.button_color);
                        startRepeatingTask();
                        start_btn.setEnabled(false);
                        start_btn.getBackground().setAlpha(100);

                        stop_btn.setEnabled(true);
                        stop_btn.getBackground().setAlpha(200);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else if (serviceCode == ServiceCode.STOP_BUTTON) {
                AppLog.Log("STOP_BUTTON", getResponse);

                try {
                    JSONObject jsonObject = new JSONObject(getResponse);
                    JSONObject newJsonObj = jsonObject.getJSONObject(JsonParseKey.TRACK_STOP_RESULT);
                    String getMessage = newJsonObj.getString(JsonParseKey.MESSAGE);
                    int status = newJsonObj.getInt(JsonParseKey.STATUS);
                    if(status == 0) {
                        Singleton.getInstance(mContext).ShowSnackMessage(getMessage, coordinatorLayout, R.color.button_color);
                    } else {
                        JSONObject resObj = newJsonObj.getJSONObject(JsonParseKey.RESPONSE);
                        String trackId = resObj.getString(JsonParseKey.ID);
                        //String getInc = comment_edt.getText().toString();
                        //String getSelectedSite = select_site_btn.getText().toString();
                        comment_edt.setText("");
                        select_site_btn.setText(getResources().getString(R.string.lbl_select_site));
                        Singleton.getInstance(mContext).selectedSiteId = null;
                        Singleton.getInstance(mContext).getSelectedSiteName = null;
                        sessionManager.createLoginSession(yourTrackerUserDetails.get(SessionManager.KEY_ROLE),
                                yourTrackerUserDetails.get(SessionManager.KEY_TOKEN), yourTrackerUserDetails.get(SessionManager.KEY_STATUS),
                                yourTrackerUserDetails.get(SessionManager.KEY_TIME_FRAME), trackId, "No", null,
                                null, null);
                        Singleton.getInstance(mContext).ShowSnackMessage("Your service has been stoped", coordinatorLayout, R.color.button_color);
                        stopRepeatingTask();

                        start_btn.setEnabled(true);
                        start_btn.getBackground().setAlpha(200);

                        stop_btn.setEnabled(false);
                        stop_btn.getBackground().setAlpha(100);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else if (serviceCode == ServiceCode.LOGOUT) {
                AppLog.Log("LOGOUT", getResponse);
                try {
                    JSONObject jsonObject = new JSONObject(getResponse);
                    JSONObject logJsonObj = jsonObject.getJSONObject(JsonParseKey.LOG_OUT_RESULT);
                    String msg = logJsonObj.getString(JsonParseKey.MESSAGE);
                    if(msg != null && msg.equalsIgnoreCase("Successful")) {
                        Singleton.getInstance(mContext).ShowSnackMessage("Successfully logout", coordinatorLayout, R.color.button_color);
                        sessionManager.logoutUser();
                    } else {
                        Singleton.getInstance(mContext).ShowSnackMessage("Failed to logout, please try again", coordinatorLayout, R.color.button_color);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }  else if (serviceCode == ServiceCode.SEND_LOCATION) {
                AppLog.Log("SEND_LOCATION", getResponse);
            } else {
                AppLog.Log("Else", getResponse);
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


    /*
    TODO SERVICE NAME
     */

    private void launchLoadSiteTask(String url, int ServiceTypeCode) {
        AppLog.Log("url>>", url);
        HttpService httpService = new HttpService(mContext, this, ServiceTypeCode, false);
        httpService.execute(url);
    }

    private void launchStartStopTask(String url, int ServiceTypeCode) {
        if (!Connectivity.isConnected(mContext)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_error_network), coordinatorLayout
            ,R.color.button_color);
            return;
        }
        AppLog.Log("url>>", url);
        HttpService httpService = new HttpService(mContext, this, ServiceTypeCode, true);
        httpService.execute(url);
    }

    private void launchSendPeriodicallyTask(String url, int ServiceTypeCode) {
        if (!Connectivity.isConnected(mContext)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_error_network), coordinatorLayout, R.color.button_color);
            return;
        }
        AppLog.Log("url>>", url);
        HttpService httpService = new HttpService(mContext, this, ServiceTypeCode, false);
        httpService.execute(url);
    }

    private void launchLogoutTask(String url, int ServiceTypeCode) {
        if (!Connectivity.isConnected(mContext)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_error_network), coordinatorLayout, R.color.button_color);
            return;
        }
        AppLog.Log("url>>", url);
        HttpService httpService = new HttpService(mContext, this, ServiceTypeCode, true);
        httpService.execute(url);
    }




    @Override
    public void onConnected(@Nullable Bundle bundle) {
        AppLog.Log(SelectSite.class.getSimpleName(), "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if(lastLocation != null) {

                lat = lastLocation.getLatitude();
                lon = lastLocation.getLongitude();
                AppLog.Log("LATLNG: ", lat +" ," +lon);
                //GetNearByEventService(lat, lon);
            } else {
                showSettingsAlert();
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        AppLog.Log(SelectSite.class.getSimpleName(), "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if(lastLocation != null) {

                lat = lastLocation.getLatitude();
                lon = lastLocation.getLongitude();

                AppLog.Log("LATLNG: ", lat +" ," +lon);
                //GetNearByEventService(lat, lon);
            } else {
                //showSettingsAlert();
            }

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AppLog.Log(SelectSite.class.getSimpleName(), "Can't connect to Google Play Services!");
    }

    private String getDateTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmm");
        String strDate = sdf.format(cal.getTime());
        System.out.println("Current date in String Format: " + strDate);

        SimpleDateFormat sdf1 = new SimpleDateFormat();
        sdf1.applyPattern("ddMMyyyyHHmm");
        Date date = null;
        try {
            date = sdf1.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String string=sdf1.format(date);
        System.out.println("Current date in Date Format: " + string);

        return string;
    }
}
