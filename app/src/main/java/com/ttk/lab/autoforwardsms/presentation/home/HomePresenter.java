package com.ttk.lab.autoforwardsms.presentation.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ttk.lab.autoforwardsms.Constants;
import com.ttk.lab.autoforwardsms.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HomePresenter implements IHomePresenter {

    private IHomeView homeView;
    private Context mContext;
    private final int TIMEOUT_CONNECT = 5000;
    private final int RESPOND_CODE_SUCCESS = 200;

    HomePresenter (IHomeView homeView, Context context){
        this.homeView = homeView;
        this.mContext = context;
    }

    @Override
    public void getChatID(String token) {
        new GetChatIDByToken().execute(token);
    }

    @Override
    public void testTelegramConnect(String[] args) {
        new checkTelegramConnect().execute(args);
    }

    @Override
    public void checkValidPhoneNumber(String phone) {
        Pattern p = Pattern.compile(Constants.PHONE_NUMBER_PATTERN);
        Matcher matcher = p.matcher(phone);
        homeView.showPhoneValid(matcher.find());
    }

    @SuppressLint("StaticFieldLeak")
    private class checkTelegramConnect extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            homeView.showLoading(true);
        }

        @Override
        protected Void doInBackground(String... msgs) {
            String msg = Uri.encode(mContext.getString(R.string.test_msg));
            String api = String.format(Constants.TELEGRAM.API, msgs[0], msgs[1], msg);
            try {
                URL url = new URL(api);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_CONNECT);
                conn.setReadTimeout(TIMEOUT_CONNECT);
                conn.setRequestMethod("POST");
                conn.connect();
                if (conn.getResponseCode() == RESPOND_CODE_SUCCESS) {
                    Log.d(Constants.TAG, "forward SMS via telegram success");
                    homeView.showNotification(Constants.NOTI_TYPE.SUCCESS, "Connect with Telegram success");
                } else {
                    Log.d(Constants.TAG, conn.getResponseCode() + " " + conn.getResponseMessage());
                    homeView.showNotification(Constants.NOTI_TYPE.ERROR, conn.getResponseCode() + " " + conn.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            homeView.showLoading(false);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetChatIDByToken extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            homeView.showLoading(true);
        }

        @Override
        protected String doInBackground(String... msgs) {
            String api = String.format(Constants.TELEGRAM.UPDATE_API, msgs[0]);
            try {
                URL url = new URL(api);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_CONNECT);
                conn.setReadTimeout(TIMEOUT_CONNECT);
                conn.setRequestMethod("POST");
                conn.connect();
                if (conn.getResponseCode() == RESPOND_CODE_SUCCESS) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    Pattern p = Pattern.compile(Constants.TELEGRAM.CHAT_ID_PATTERN);
                    Matcher matcher = p.matcher(sb.toString());
                    if (matcher.find()) {
                        return matcher.group(1);
                    } else {
                        Log.d(Constants.TAG, sb.toString());
                        homeView.showNotification(Constants.NOTI_TYPE.ERROR, mContext.getString(R.string.error_cannot_find_chat_id));
                    }
                } else {
                    homeView.showNotification(Constants.NOTI_TYPE.ERROR, mContext.getString(R.string.error_token_invalid));
                }
            } catch (IOException e) {
                e.printStackTrace();
                homeView.showNotification(Constants.NOTI_TYPE.ERROR, mContext.getString(R.string.error_internet_connection));
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            homeView.showLoading(false);
            homeView.updateChatID(s);
        }
    }
}
