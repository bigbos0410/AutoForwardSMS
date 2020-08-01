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
    public void testTelegramConnect() {
        new checkTelegramConnect().execute("sdfghjkl");
    }

    @Override
    public void checkValidPhoneNumber(String phone) {
        Pattern p = Pattern.compile(Constants.PHONE_NUMBER_PATTERN);
        Matcher matcher = p.matcher(phone);
        homeView.showPhoneValid(matcher.find());
    }

    private class checkTelegramConnect extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            homeView.showLoading(true);
        }

        @Override
        protected Boolean doInBackground(String... msgs) {
            String msg = Uri.encode(mContext.getString(R.string.test_msg));
            String api = String.format(Constants.TELEGRAM.API, Constants.TELEGRAM.TOKEN, Constants.TELEGRAM.CHAT_ID, msg);
            try {
                URL url = new URL(api);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_CONNECT);
                conn.setReadTimeout(TIMEOUT_CONNECT);
                conn.setRequestMethod("POST");
                conn.connect();
                if (conn.getResponseCode() == RESPOND_CODE_SUCCESS) {
                    Log.d(Constants.TAG, "forward SMS via telegram success");
                    return true;
                } else {
                    Log.d(Constants.TAG, conn.getResponseCode() + " " + conn.getResponseMessage());
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
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
                        return "Error:" + mContext.getString(R.string.error_cannot_find_chat_id);
                    }
                } else {
                    return "Error:" + mContext.getString(R.string.error_token_invalid);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error:" + mContext.getString(R.string.error_internet_connection);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            homeView.showLoading(false);
            if (s != null && !s.contains("Error:")) {
                homeView.updateChatID(s);
                homeView.showErrorToken(null);
            } else {
                String error_log = s.split(":")[1];
                homeView.updateChatID("");
                homeView.showErrorToken(error_log);
            }

        }
    }
}
