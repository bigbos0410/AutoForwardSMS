package com.ttk.lab.autoforwardsms;

public interface Constants {
    String TAG = "ttk_auto_sms";
    String PHONE_NUMBER_PATTERN = "^[+]?[0-9]{10,13}$";

    interface TELEGRAM {
        String UPDATE_API = "https://api.telegram.org/bot%s/getUpdates";
        String API = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
        String CHAT_ID_PATTERN = "\"id\":(.+?),";
    }

    interface PREF {
        String PREF_NAME = "auto_forward_sms";
        String TOKEN_PREF = "token";
        String CHAT_ID_PREF = "chat_id";
        String PHONE_NUMBER_PREF = "phone_number";
        String PHONE_OPTION = "phone_option";
        String ENABLE_TELE = "enable_tele";
        String ENABLE_PHONE = "enable_phone";
    }
}
