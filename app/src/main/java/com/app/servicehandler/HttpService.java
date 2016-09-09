package com.app.servicehandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.app.interfaces.WebServiceInterface;
import com.app.tracker.R;

import java.util.Arrays;

public class HttpService extends AsyncTask<String, String, String> {

    private WebServiceInterface<Object> mCallback;
    private Context mContext;
    private ProgressDialog progressDialog = null;
    private int serviceType = 0;
    private boolean isWantProgressBar = false;

    public HttpService(Context context, WebServiceInterface<Object> callback, int serviceType, boolean isWantProgressBar) {
        this.mCallback = callback;
        this.mContext = context;
        this.serviceType = serviceType;
        this.isWantProgressBar = isWantProgressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (isWantProgressBar) {
            if (progressDialog == null && mContext != null) {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(mContext.getResources().getString(R.string.lbl_wait));
                progressDialog.show();
            }
        }
    }

    @Override
    protected String doInBackground(String... strings) {

        return ServiceUtility.getDataFromServer(strings[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        System.out.println("on Post execute called");
        mCallback.requestCompleted(s, serviceType);
    }

}
