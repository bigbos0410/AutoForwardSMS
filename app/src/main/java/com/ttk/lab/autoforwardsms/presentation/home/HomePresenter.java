package com.ttk.lab.autoforwardsms.presentation.home;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ttk.lab.autoforwardsms.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HomePresenter implements IHomePresenter {

    private IHomeView homeView;

    HomePresenter (IHomeView homeView){
        this.homeView = homeView;
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
            String msg = Uri.encode("Hello, this is test message from Forward sms application");
            String api = String.format(Constants.TELEGRAM.API, Constants.TELEGRAM.TOKEN, Constants.TELEGRAM.CHAT_ID, msg);
            try {
                URL url = new URL(api);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.connect();
                if (conn.getResponseCode() == 200) {
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
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.connect();
                if (conn.getResponseCode() == 200) {
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
                        return "Error:Cannot found ChatID, you need chat to your Bot first";
                    }
                } else {
                    return "Error:Token invalid";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error:" + e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            homeView.showLoading(false);
            if (s != null && !s.contains("Error:")) {
                homeView.updateChatID(s);
                homeView.enableTestTelegram(true);
                homeView.showError(null);
            } else {
                String error_log = s.split(":")[1];
                homeView.updateChatID("");
                homeView.showError(error_log);
                homeView.enableTestTelegram(false);
            }

        }
    }
}
