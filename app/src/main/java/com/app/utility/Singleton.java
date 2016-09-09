package com.app.utility;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.app.tracker.R;

/**
 * Created by VIJAY on 02-09-2016.
 */
public class Singleton {

    private static volatile Singleton instance = null;
    private Context context;

    public String selectedSiteId = null;
    public String getSelectedSiteName = null;


    private Singleton(Context context) {
        this.context = context;
    }

    public static Singleton getInstance(Context context) {
        if (instance == null) {
            synchronized (Singleton.class) {
                instance = new Singleton(context);
            }
        }
        return instance;
    }

  /*  public static void setInstance(UtilitySingleton instance) {
        UtilitySingleton.instance = instance;
    }*/


    // --------- show Message in whole application-----------

    public void ShowSnackMessage(String msg, CoordinatorLayout coordinatorLayout, int color) {
        if (msg != null && !msg.trim().equalsIgnoreCase("")) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(context, color));

            snackbar.show();
        }
    }

    public void ShowToastMessage(String msg, Context ctx) {
        if (msg != null && !msg.trim().equalsIgnoreCase("")) {
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();

        }
    }

}
