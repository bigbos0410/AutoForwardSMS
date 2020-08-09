package com.ttk.lab.autoforwardsms.presentation.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.tapadoo.alerter.Alerter;
import com.ttk.lab.autoforwardsms.Constants;
import com.ttk.lab.autoforwardsms.PreferenceHelper;
import com.ttk.lab.autoforwardsms.R;
import com.ttk.lab.autoforwardsms.databinding.ActivityHomeBinding;
import com.ttk.lab.autoforwardsms.presentation.guide.GuideActivity;

public class HomeView extends AppCompatActivity implements IHomeView, CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener {

    private AdView mAdView;
    IHomePresenter homePresenter;
    ActivityHomeBinding mBinding;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        MobileAds.initialize(this, initializationStatus -> {});
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void initLayout(){
        homePresenter = new HomePresenter(this, this);
        mPreferences = getSharedPreferences(Constants.PREF.PREF_NAME, MODE_PRIVATE);
        String token = mPreferences.getString(Constants.PREF.TOKEN_PREF, "");
        enableTestTelegram(!token.equals(""));
        mBinding.edToken.setText(token);
        mBinding.edChatId.setText(mPreferences.getString(Constants.PREF.CHAT_ID_PREF, ""));
        mBinding.edPhoneNumber.setText(mPreferences.getString(Constants.PREF.PHONE_NUMBER_PREF, ""));
        if (checkPermission()) {
            mBinding.swTele.setChecked(mPreferences.getBoolean(Constants.PREF.ENABLE_TELE, false));
            mBinding.swPhone.setChecked(mPreferences.getBoolean(Constants.PREF.ENABLE_PHONE, false));
        } else {
            PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_TELE, false);
            PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_PHONE, false);
            mBinding.swTele.setChecked(false);
            mBinding.swPhone.setChecked(false);
        }
        updatePhoneOptionUI(mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0));

        mBinding.swTele.setOnCheckedChangeListener(this);
        mBinding.swPhone.setOnCheckedChangeListener(this);

        mBinding.edToken.setOnFocusChangeListener(this);
        mBinding.edPhoneNumber.setOnFocusChangeListener(this);

        mBinding.edlToken.setEndIconOnClickListener(view -> {
            if (mBinding.edToken.isFocused()) {
                mBinding.edlToken.clearFocus();
            } else {
                mBinding.edlToken.requestFocus();
                mBinding.edToken.selectAll();
            }
        });
        mBinding.edlPhoneNumber.setEndIconOnClickListener(view -> {
            if (mBinding.edPhoneNumber.isFocused()) {
                mBinding.edlPhoneNumber.clearFocus();
            } else {
                mBinding.edlPhoneNumber.requestFocus();
                mBinding.edPhoneNumber.selectAll();
            }
        });
    }

    boolean checkPermission () {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {
            showDialogOpenSetting();
            return false;
        } else {
            requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS },
                    99);
            return false;
        }
    }

    void showDialogOpenSetting(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeView.this);
        alertDialog.setMessage(getString(R.string.permission_request_failed_noti)).setPositiveButton(getString(R.string.go_to_setting), (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getApplication().getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(intent);
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
            if (grantResults.length == 0 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showDialogOpenSetting();
            }
        }
    }


    public void testTelegramConnect(View view){
        String[] args = {String.valueOf(mBinding.edToken.getText()), String.valueOf(mBinding.edChatId.getText())};
        homePresenter.testTelegramConnect(args);
    }

    public void connectTeleGuide(View view){
        Intent intent = new Intent(HomeView.this, GuideActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateChatID(String chat_id) {
        if (!chat_id.isEmpty()) {
            mBinding.edChatId.setText(chat_id);
            showNotification(Constants.NOTI_TYPE.SUCCESS, "Get ChatID success. Saved");
            PreferenceHelper.putString(mPreferences, Constants.PREF.TOKEN_PREF, String.valueOf(mBinding.edToken.getText()));
            PreferenceHelper.putString(mPreferences, Constants.PREF.CHAT_ID_PREF, String.valueOf(mBinding.edChatId.getText()));
            enableTestTelegram(true);
        } else {
            mBinding.edChatId.setText("");
            enableTestTelegram(false);
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (loading) {
            mBinding.layoutScreen.setClickable(false);
            mBinding.layoutScreen.setAlpha(0.3f);
            mBinding.loading.setVisibility(View.VISIBLE);
        } else {
            mBinding.layoutScreen.setClickable(true);
            mBinding.layoutScreen.setAlpha(1f);
            mBinding.loading.setVisibility(View.GONE);
        }
    }

    public void enableTestTelegram(boolean enable) {
        mBinding.btnTextToTelegram.setEnabled(enable);
        mBinding.btnTextToTelegram.setAlpha(enable ? 1f : 0.3f);
    }

    @Override
    public void showPhoneValid(boolean isValid) {
        if (isValid) {
            showNotification(Constants.NOTI_TYPE.SUCCESS, "Save phone number");
            PreferenceHelper.putString(mPreferences, Constants.PREF.PHONE_NUMBER_PREF, String.valueOf(mBinding.edPhoneNumber.getText()));
        } else {
            showNotification(Constants.NOTI_TYPE.ERROR, getString(R.string.invalid_phone_number_error));
        }
    }

    @SuppressLint("ResourceAsColor")
    void updatePhoneOptionUI(int option) {
        String text;
        String more = String.valueOf(getText(R.string.more_option));
        if (option == 0) {
            text = getText(R.string.only_send_if_telegram_not_available) + " ";
        } else {
            text = getText(R.string.always_send_to_phone) + " ";
        }
        Spannable spannable = new SpannableString(text + more);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeView.this);
                String[] items = {getString(R.string.only_send_if_telegram_not_available), getString(R.string.always_send_to_phone)};
                alertDialog.setSingleChoiceItems(items,  mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0), (dialog, option1) -> {
                    int current_option = mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0);
                    if (option1 != current_option) {
                        PreferenceHelper.putInt(mPreferences, Constants.PREF.PHONE_OPTION, option1);
                        Toast.makeText(getApplicationContext(), getString(R.string.saved), Toast.LENGTH_LONG).show();
                        updatePhoneOptionUI(option1);
                    }
                }).setPositiveButton("OK", null);
                AlertDialog alert = alertDialog.create();
                alert.show();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getColor(R.color.colorPrimary));
            }
        };
        spannable.setSpan(clickableSpan, text.length(), (text + more).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.tvSendToPhoneOption.setText(spannable, TextView.BufferType.SPANNABLE);
        mBinding.tvSendToPhoneOption.setMovementMethod(LinkMovementMethod.getInstance());
    }

    //perform onCheckedChanged of switch
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        switch (id) {
            case R.id.sw_tele:
                if (b) {
                    if (checkPermission()) {
                        String token = mPreferences.getString(Constants.PREF.TOKEN_PREF, "");
                        if (token.equals("")) {
                            mBinding.swTele.setChecked(false);
                            showNotification(Constants.NOTI_TYPE.WARNING, getString(R.string.need_valid_token));
                        } else {
                            PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_TELE, true);
                            mBinding.edToken.setText(token);
                            mBinding.edChatId.setText(mPreferences.getString(Constants.PREF.CHAT_ID_PREF, ""));
                            mBinding.edToken.selectAll();
                        }
                    } else {
                        mBinding.swTele.setChecked(false);
                    }
                } else {
                    PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_TELE, false);
                }
                break;
            case R.id.sw_phone:
                if (b) {
                    if (checkPermission()) {
                        String phone_number = mPreferences.getString(Constants.PREF.PHONE_NUMBER_PREF, "");
                        if (phone_number.equals("")) {
                            mBinding.swPhone.setChecked(false);
                            showNotification(Constants.NOTI_TYPE.WARNING, getString(R.string.need_valid_phone));
                        } else {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeView.this);
                            alertDialog.setMessage(getString(R.string.alert_sms_enable)).setPositiveButton(getString(R.string.understood), null);
                            AlertDialog alert = alertDialog.create();
                            alert.show();
                            PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_PHONE, true);
                            mBinding.edPhoneNumber.setText(phone_number);
                            mBinding.edPhoneNumber.selectAll();
                        }
                    } else {
                        mBinding.swPhone.setChecked(false);
                    }
                } else {
                    PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_PHONE, false);
                }
                break;
        }
    }

    //perform onFocusChangeListener on EditText
    @Override
    public void onFocusChange(View view, boolean b) {
        int id = view.getId();
        showAd(!b);
        if (!b) {
            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        switch (id) {
            case R.id.ed_token:
                if (!b) {
                    if (!String.valueOf(mBinding.edToken.getText()).equals("")) {
                        homePresenter.getChatID(String.valueOf(mBinding.edToken.getText()));
                    }
                    mBinding.edlToken.setEndIconDrawable(R.drawable.ic_edit);
                } else {
                    mBinding.edlToken.setEndIconDrawable(R.drawable.ic_check);
                }
                break;
            case R.id.ed_phone_number:
                if (!b) {
                    if (!String.valueOf(mBinding.edPhoneNumber.getText()).equals("")) {
                        homePresenter.checkValidPhoneNumber(String.valueOf(mBinding.edPhoneNumber.getText()));
                    }
                    mBinding.edlPhoneNumber.setEndIconDrawable(R.drawable.ic_edit);
                } else {
                    mBinding.edlPhoneNumber.setEndIconDrawable(R.drawable.ic_check);
                }
                break;
        }
    }

    @Override
    public void showNotification(int code, String msg) {
        int color;
        switch (code) {
            case Constants.NOTI_TYPE.ERROR:
                color = R.color.noti_error;
                break;
            case Constants.NOTI_TYPE.SUCCESS:
                color = R.color.noti_success;
                break;
            default:
                color = R.color.noti_warning;
        }
        Alerter.create(this)
                .setText(msg)
                .setBackgroundColor(color)
                .show();
    }

    void showAd(boolean show) {
        if (show) {
            mBinding.adView.setVisibility(View.VISIBLE);
        } else {
            mBinding.adView.setVisibility(View.GONE);
        }
    }
}

