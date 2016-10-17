package com.app.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SelectSite extends AppCompatActivity implements WebServiceInterface,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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

    private int getTimeFrame;
    private String _getTimeFrame = null;

    private GoogleApiClient googleApiClient;
    private final int LOCATION_RESOLVER_CODE = 1000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 2000;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 2000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    Handler mHandler = new Handler();

    private String getTrackID = null;
    //private String getSelectedSiteName = null;
    //private String getSelectedSiteId = null;
    private String getSelectedIncident = null;
    private boolean isGPSCheckStarted = false;
    private Handler handler = new Handler();

    private TimerTask doThis;

    private boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_site);

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }
        initWidget();
        loadWebsites();

        String serviceStatus = yourTrackerUserDetails.get(SessionManager.KEY_SERVICE_STATUS);

        if (!Validation.isNullOrEmpty(getSelectedIncident)) {
            comment_edt.setText(getSelectedIncident);
        } else {
            comment_edt.setText("");
        }

        AppLog.Log("serviceStatus: ", serviceStatus + "");

        if (serviceStatus != null && serviceStatus.equalsIgnoreCase("Yes")) {
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


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    /*
    Method to display the location on UI
     */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }

        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 11);
        }


        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(googleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Singleton.getInstance(mContext).longitude = longitude;
            Singleton.getInstance(mContext).latitude = latitude;

            AppLog.Log("Location: ", latitude + ", " + longitude);

        } else {

            LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            displayLocation();
                            AppLog.Log("LocationSettingsStatusCodes", "Success");
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                AppLog.Log("LocationSettingsStatusCodes", "RESOLUTION_REQUIRED");
                                status.startResolutionForResult(SelectSite.this, LOCATION_RESOLVER_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            AppLog.Log("LocationSettingsStatusCodes", "SETTINGS_CHANGE_UNAVAILABLE");
                            break;
                    }
                }
            });

            AppLog.Log("Location: ", "(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Method to toggle periodic location updates
     */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            AppLog.Log("togglePeriodicLocationUpdates: ", "Periodic location updates started!");

        } else {
            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            AppLog.Log("togglePeriodicLocationUpdates", "Periodic location updates stopped!");
        }
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 10);
        }

        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 11);
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    void startRepeatingTask() {
        scheduleSendLocation();
    }

    void stopRepeatingTask() {
        if (doThis != null) {
            doThis.cancel();
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
        if (!Validation.isNullOrEmpty(_getTimeFrame)) {
            getTimeFrame = Integer.valueOf(_getTimeFrame); //1000 * 60 * 2;
            getTimeFrame = 1000 * 60 * getTimeFrame;
            AppLog.Log("getTimeFrame: ", "Min: " + _getTimeFrame + "TimeMil: " + getTimeFrame + "");
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

                AppLog.Log("Selected: ", sites.getId() + " " + sites.getName());
                Singleton.getInstance(mContext).selectedSiteId = sites.getId();
                if (Singleton.getInstance(mContext).getSelectedSiteName == null) {
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

/*    @Override
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
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppLog.Log("Called", "onStop");
        //googleApiClient.disconnect();
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

        if (getSelectedSite != null && getSelectedSite.equalsIgnoreCase(getResources().getString(R.string.lbl_select_site))) {
            Singleton.getInstance(mContext).ShowSnackMessage("Please select site", coordinatorLayout, R.color.button_color);

            return;
        }

        if (selectedSiteId == null || Integer.valueOf(selectedSiteId) == 0) {
            selectedSiteId = Singleton.getInstance(mContext).selectedSiteId;
        }

        AppLog.Log("Selected_site: ", selectedSiteId);
        if (Validation.isNullOrEmpty(selectedSiteId) ||
                selectedSiteId.equalsIgnoreCase("0")) {
            Singleton.getInstance(mContext).ShowSnackMessage("Please select site", coordinatorLayout, R.color.button_color);
            return;
        }
        if (Validation.isNullOrEmpty(getIncident)) {
            Singleton.getInstance(mContext).ShowSnackMessage("Please enter incident", coordinatorLayout, R.color.button_color);
            return;
        }

        if (Singleton.getInstance(mContext).latitude == 0 || Singleton.getInstance(mContext).longitude == 0) {
            Singleton.getInstance(mContext).ShowToastMessage("Please enable location from device ", mContext);
            return;
        }
        String compUrl = ApiUrl.START_SERVICE + "/" + getToken + "/" + Constants.START_SWITCH + "/" +
                getDate.replaceAll(" ", "%20") + "/" +
                selectedSiteId + "/" +
                getIncident.replaceAll(" ", "%20") + "/" + Singleton.getInstance(mContext).latitude + "/" + Singleton.getInstance(mContext).longitude;
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

        if (trackId == null) {
            trackId = getTrackID;
        }
      /*  if(Validation.isNullOrEmpty(getIncident)) {
            Singleton.getInstance(mContext).ShowToastMessage("Please enter incident", mContext);
            return;
        }*/

        if (Singleton.getInstance(mContext).latitude == 0 || Singleton.getInstance(mContext).longitude == 0) {
            Singleton.getInstance(mContext).ShowToastMessage("Please enable location from device ", mContext);
            return;
        }
        String compUrl = ApiUrl.STOP_SERVICE + "/" + getToken + "/" +
                trackId + "/" +
                getDate.replaceAll(" ", "%20") +
                "/" + Singleton.getInstance(mContext).latitude + "/" + Singleton.getInstance(mContext).longitude;
        launchStartStopTask(compUrl, ServiceCode.STOP_BUTTON);
    }

    public void appLogout(View view) {
        String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
        String compUrl = ApiUrl.LOGOUT + "/" + getToken;
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
                    if (status == 0) {
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

            } else if (serviceCode == ServiceCode.STOP_BUTTON) {
                AppLog.Log("STOP_BUTTON", getResponse);

                try {
                    JSONObject jsonObject = new JSONObject(getResponse);
                    JSONObject newJsonObj = jsonObject.getJSONObject(JsonParseKey.TRACK_STOP_RESULT);
                    String getMessage = newJsonObj.getString(JsonParseKey.MESSAGE);
                    int status = newJsonObj.getInt(JsonParseKey.STATUS);
                    if (status == 0) {
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

            } else if (serviceCode == ServiceCode.LOGOUT) {
                AppLog.Log("LOGOUT", getResponse);
                try {
                    JSONObject jsonObject = new JSONObject(getResponse);
                    JSONObject logJsonObj = jsonObject.getJSONObject(JsonParseKey.LOG_OUT_RESULT);
                    String msg = logJsonObj.getString(JsonParseKey.MESSAGE);
                    if (msg != null && msg.equalsIgnoreCase("Successful")) {
                        Singleton.getInstance(mContext).ShowSnackMessage("Successfully logout", coordinatorLayout, R.color.button_color);
                        sessionManager.logoutUser();
                    } else {
                        Singleton.getInstance(mContext).ShowSnackMessage("Failed to logout, please try again", coordinatorLayout, R.color.button_color);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (serviceCode == ServiceCode.SEND_LOCATION) {
                Calendar c = Calendar.getInstance();
                int min = c.get(Calendar.MINUTE);
                int hour = c.get(Calendar.HOUR);
                AppLog.Log("SEND_LOCATION", hour + ": " + min + " " + getResponse);
            } else {
                AppLog.Log("Else", getResponse);
            }
        }
    }


 /*   @Override
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
    }*/


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
                    , R.color.button_color);
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
        AppLog.Log(Splash.class.getSimpleName(), "Connected to Google Play Services!");

        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        AppLog.Log("Called: ", "onActivityResult");

        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        AppLog.Log("resultCode", resultCode + " ," + requestCode);
        switch (requestCode) {
            case LOCATION_RESOLVER_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        displayLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Singleton.getInstance(mContext).ShowToastMessage("Did you forgot to open GPS ", mContext);
                        break;
                    default:
                        break;
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppLog.Log("Called: ", "onResume");
        checkPlayServices();

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

    private String getDateTime() {
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
        String string = sdf1.format(date);
        System.out.println("Current date in Date Format: " + string);

        return string;
    }


    private void scheduleSendLocation() {
        int delay = 0;   // delay for 30 sec.
        int period = getTimeFrame;  // repeat every 60 sec.
        Timer myTimer = new Timer();
        doThis = new TimerTask() {
            public void run() {
                AppLog.Log("getTimeFrame: ", "Min: " + _getTimeFrame + "TimeMil: " + getTimeFrame + "");

                String getToken = yourTrackerUserDetails.get(SessionManager.KEY_TOKEN);
                String trackId = yourTrackerUserDetails.get(SessionManager.KEY_TRACK_ID);
                if (getTrackID == null) {
                    getTrackID = trackId;
                }
                String getDate = getDateTime();

                if (Validation.isNullOrEmpty(getToken)) {
                    sessionManager.logoutUser();
                    Singleton.getInstance(mContext).ShowSnackMessage("Session expired, please login again", coordinatorLayout, R.color.button_color);
                    return;
                }

                if (Validation.isNullOrEmpty(getTrackID)) {

                    Singleton.getInstance(mContext).ShowSnackMessage("Please try again", coordinatorLayout, R.color.button_color);
                    return;
                }

                if (Singleton.getInstance(mContext).latitude == 0 || Singleton.getInstance(mContext).longitude == 0) {
                    Singleton.getInstance(mContext).ShowToastMessage("Please enable location from device ", mContext);
                    return;
                }

                String compUrl = ApiUrl.SEND_LOCATION + "/" + getToken + "/" + getTrackID + "/" + getDate + "/" +
                        Singleton.getInstance(mContext).latitude + "/" + Singleton.getInstance(mContext).longitude;
                launchSendPeriodicallyTask(compUrl, ServiceCode.SEND_LOCATION);
            }
        };

        myTimer.scheduleAtFixedRate(doThis, delay, period);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        // Displaying the new location on UI
        displayLocation();
    }

   /* private void exitApp() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);

        // set title
        alertDialogBuilder.setTitle("Exit");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you really want to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity

                        //SelectSite.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            AppLog.Log("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        AppLog.Log("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}
