package com.app.tracker;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.app.utility.SessionManager;

public class Splash extends AppCompatActivity {

    private Context mContext;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // hide statusbar of Android
        // could also be done later
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mContext = Splash.this;
        sessionManager = new SessionManager(getApplicationContext());


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //userSession.checkLogin();
                Intent intent;
                if(sessionManager.isLoggedIn() && mContext != null) {
                    //SplashView.this.finish();
                    intent = new Intent(mContext,SelectSite.class);
                    //finish();
                } else {
                    intent = new Intent(mContext,Login.class);
                    //finish();
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //finish();
                startActivity(intent);


            }
        }, 2000);

    }
}
