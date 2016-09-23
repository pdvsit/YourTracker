package com.app.tracker;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.interfaces.WebServiceInterface;
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

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements WebServiceInterface {
    private Context mContext;
    private EditText edt_login;
    private ProgressBar loader;

    private CoordinatorLayout coordinatorLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initWidget();

    }


    private void initWidget() {
        mContext = Login.this;
        sessionManager = new SessionManager(mContext);
        edt_login = (EditText) this.findViewById(R.id.edt_login);
        coordinatorLayout = (CoordinatorLayout) this.findViewById(R.id.coordinatorLayout);
        loader = (ProgressBar) this.findViewById(R.id.loader);


        edt_login.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    AppLog.Log("TAG: ","Enter pressed");

                    String getUserInput = edt_login.getText().toString();
                    if(Validation.isNullOrEmpty(getUserInput)) {
                        Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.error_email), coordinatorLayout,
                                R.color.button_color);
                    } else if (!Connectivity.isConnected(mContext)) {
                        Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_error_network), coordinatorLayout, R.color.button_color);
                    } else {
                        loader.setVisibility(View.VISIBLE);
                        String deviceId = Settings.Secure.getString(getApplication().getContentResolver(),
                                Settings.Secure.ANDROID_ID);

                        String compUrl = ApiUrl.LOGIN + "/" + getUserInput + "/" + Constants.PASSWORD + "/" + deviceId + "/" + deviceId;
                        launchLoginTask(compUrl, ServiceCode.LOGIN);
                    }
                }
                return false;
            }
        });

    }

    public void myFancyMethod(View v) {
        // does something very interesting
        String getUserInput = edt_login.getText().toString();
        if(Validation.isNullOrEmpty(getUserInput)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.error_email), coordinatorLayout,
                    R.color.button_color);
            return;
        }

      /*  if(!Validation.isEmailValid(getUserInput)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.error_valid_email), coordinatorLayout);
            return;
        }*/

        if (!Connectivity.isConnected(mContext)) {
            Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_error_network), coordinatorLayout, R.color.button_color);
            return;
        }
        loader.setVisibility(View.VISIBLE);
        String deviceId = Settings.Secure.getString(getApplication().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String compUrl = ApiUrl.LOGIN +"/"+ getUserInput +"/"+ Constants.PASSWORD +"/"+ deviceId +"/"+ deviceId;
        launchLoginTask(compUrl, ServiceCode.LOGIN);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mContext != null) {
            Login.this.finish();
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

    @Override
    public void requestCompleted(Object obj, int serviceCode) {

        if(obj != null) {
            String getResponse = obj.toString();
            if(serviceCode == ServiceCode.LOGIN) {
                loader.setVisibility(View.GONE);
                AppLog.Log("Login_getResponse: ", getResponse);
                if(getResponse.contains(JsonParseKey.TOKEN)) {

                    try {
                        JSONObject jsonObject = new JSONObject(getResponse);

                        JSONObject resultJsonObj = jsonObject.getJSONObject(JsonParseKey.LOGIN_RESULT);
                        String message = resultJsonObj.getString(JsonParseKey.MESSAGE);
                        String status = resultJsonObj.getString(JsonParseKey.STATUS);

                        JSONObject resJsonObj = resultJsonObj.getJSONObject(JsonParseKey.RESPONSE);
                        String role = resJsonObj.getString(JsonParseKey.ROLE);
                        String token = resJsonObj.getString(JsonParseKey.TOKEN);
                        String timeFrame = resJsonObj.getString(JsonParseKey.TIME_FRAME);

                        Singleton.getInstance(mContext).ShowSnackMessage(message, coordinatorLayout, R.color.button_color);
                        sessionManager.createLoginSession(role, token, status, timeFrame, null, null, null, null, null);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(getApplicationContext(), SelectSite.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }
                        }, Constants.DELAY);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Singleton.getInstance(mContext).ShowSnackMessage(getResources().getString(R.string.lbl_login_failed), coordinatorLayout, R.color.button_color);
                }
                //startActivity(new Intent(mContext, SelectSite.class));

            }
        }

    }

    private void launchLoginTask(String url, int ServiceTypeCode) {
        AppLog.Log("url>>", url);
        HttpService httpService = new HttpService(mContext, this, ServiceTypeCode, false);
        httpService.execute(url);
    }

}
