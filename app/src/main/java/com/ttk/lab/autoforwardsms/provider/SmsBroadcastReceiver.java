package com.ttk.lab.autoforwardsms.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ttk.lab.autoforwardsms.ForwardSmsAsyncTask;

import static com.ttk.lab.autoforwardsms.Constants.TAG;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                    }
                    Log.i(TAG, "Message recieved: " + messages[0].getMessageBody());
                    String msg_content = "<FROM> " + messages[0].getOriginatingAddress() + "\n" + messages[0].getMessageBody();
                    new ForwardSmsAsyncTask(context).execute(msg_content);
                }
            }
        }
    }
}
