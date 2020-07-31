package com.ttk.lab.autoforwardsms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class ForwardSmsAsyncTask extends AsyncTask<String, Void, Void> {

    Context mContext;
    SharedPreferences mPreferences;
    String token;
    String chat_id;
    String phone_number;
    boolean enable_telegram;
    boolean enable_phone;
    int phone_option;

    public ForwardSmsAsyncTask (Context mContext) {
        this.mContext = mContext;
        this.mPreferences = mContext.getSharedPreferences(Constants.PREF.PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        token = mPreferences.getString(Constants.PREF.TOKEN_PREF, "");
        chat_id = mPreferences.getString(Constants.PREF.CHAT_ID_PREF, "");
        phone_number = mPreferences.getString(Constants.PREF.PHONE_NUMBER_PREF, "");
        enable_telegram = mPreferences.getBoolean(Constants.PREF.ENABLE_TELE, false);
        enable_phone = mPreferences.getBoolean(Constants.PREF.ENABLE_PHONE, false);
        phone_option = mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0);
    }

    @Override
    protected Void doInBackground(String... msgs) {
        String msg = Uri.encode(msgs[0]);
        String api = String.format(Constants.TELEGRAM.API, token, chat_id, msg);
        Log.d(Constants.TAG, "forward_sms, setting"
                + " enable telegram: " + enable_telegram
                + " enable phone: " + enable_phone
                + " phone option: " + phone_option);
        if (enable_telegram) {
            try {
                URL url = new URL(api);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    Log.d(Constants.TAG, "Forward SMS via telegram success");
                    if (enable_phone && phone_option == 1) {
                        sendToPhone(phone_number, msg);
                    }
                } else {
                    Log.d(Constants.TAG, "Forward SMS via telegram failed, "
                            + conn.getResponseCode() + " " + conn.getResponseMessage());
                    if (enable_phone) {
                        sendToPhone(phone_number, msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.TAG, "Forward SMS via telegram failed, " + e);
                if (enable_phone) {
                    sendToPhone(phone_number, msg);
                }
            }
        } else {
            if (enable_phone) {
                sendToPhone(phone_number, msg);
            }
        }
        return null;
    }

    private void sendToPhone(String phone_number, String msg) {
        if (ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phone_number, null, msg, null, null);
            Log.d(Constants.TAG, "Forward SMS via phone number success.");
        } else {
            Log.d(Constants.TAG, "Forward SMS via phone failed. Missing permission");
        }
    }

//    @RequiresPermission(Manifest.permission.READ_SMS)
//    public static void getAllSms(Context context) {
//        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) { // must check the result to prevent exception
//            do {
//                String msg = cursor.getString(cursor.getColumnIndex("body"));
//                break;
//            } while (cursor.moveToNext());
//            cursor.close();
//        }
//    }
}
