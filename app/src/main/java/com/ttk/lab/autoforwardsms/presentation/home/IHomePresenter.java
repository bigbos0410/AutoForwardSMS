package com.ttk.lab.autoforwardsms.presentation.home;

import com.ttk.lab.autoforwardsms.presentation.BasePresenter;

public interface IHomePresenter extends BasePresenter {

    void getChatID(String token);

    void testTelegramConnect(String[] args);

    void checkValidPhoneNumber(String phone);
}
