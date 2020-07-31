package com.ttk.lab.autoforwardsms.presentation.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.ttk.lab.autoforwardsms.Constants;
import com.ttk.lab.autoforwardsms.ForwardSmsAsyncTask;
import com.ttk.lab.autoforwardsms.PreferenceHelper;
import com.ttk.lab.autoforwardsms.R;
import com.ttk.lab.autoforwardsms.databinding.ActivityHomeBinding;
import com.ttk.lab.autoforwardsms.presentation.guide.GuideActivity;

public class HomeView extends AppCompatActivity implements IHomeView {

    IHomePresenter homePresenter;
    ActivityHomeBinding mBinding;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initLayout();
    }

    void initLayout(){
        homePresenter = new HomePresenter(this);
        mPreferences = getSharedPreferences(Constants.PREF.PREF_NAME, MODE_PRIVATE);
        mBinding.edToken.setText(mPreferences.getString(Constants.PREF.TOKEN_PREF, ""));
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
        boolean enableSwitchTele = mPreferences.getBoolean(Constants.PREF.VALID_TOKEN, false);
        mBinding.swTele.setEnabled(enableSwitchTele);
        enableTestTelegram(enableSwitchTele);

        updatePhoneOptionUI(mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0));

        mBinding.swTele.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (checkPermission()) {
                    PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_TELE, true);
                } else {
                    mBinding.swTele.setChecked(false);
                }
            } else {
                PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_TELE, false);
            }
        });
        mBinding.swPhone.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (checkPermission()) {
                    PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_PHONE, true);
                } else {
                    mBinding.swPhone.setChecked(false);
                }
            } else {
                PreferenceHelper.putBoolean(mPreferences, Constants.PREF.ENABLE_PHONE, false);
            }

        });


        mBinding.edToken.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                homePresenter.getChatID(String.valueOf(mBinding.edToken.getText()));
                mBinding.edlToken.setEndIconDrawable(R.drawable.ic_edit);
            } else {
                mBinding.edlToken.setEndIconDrawable(R.drawable.ic_check);
            }
        });
        mBinding.edToken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mBinding.edlToken.setEndIconOnClickListener(view -> {
            if (mBinding.edToken.isFocused()) {
                mBinding.edlToken.clearFocus();
            } else {
                mBinding.edlToken.requestFocus();
                mBinding.edToken.selectAll();
            }
        });
        mBinding.edPhoneNumber.setOnFocusChangeListener((view, b) -> {
            if(!b) {
                mBinding.edlPhoneNumber.setEndIconDrawable(R.drawable.ic_edit);
            } else {
                mBinding.edlPhoneNumber.setEndIconDrawable(R.drawable.ic_check);
            }
        });
        mBinding.edlPhoneNumber.setEndIconOnClickListener(view -> {
            if (mBinding.edPhoneNumber.isFocused()) {
                mBinding.edlPhoneNumber.clearFocus();
                homePresenter.checkValidPhoneNumber(String.valueOf(mBinding.edPhoneNumber.getText()));
            } else {
                mBinding.edlPhoneNumber.requestFocus();
                mBinding.edPhoneNumber.selectAll();
            }
        });
        mBinding.edPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mBinding.edlPhoneNumber.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        new ForwardSmsAsyncTask(this).execute("haha");
    }

    boolean checkPermission () {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeView.this);
            alertDialog.setTitle("Permission require");
            alertDialog.setMessage("shouldShowRequestPermissionRationale").setPositiveButton("OK", null);
            AlertDialog alert = alertDialog.create();
            alert.show();
            return false;
        } else {
            requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS },
                    99);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 99:
                if (grantResults.length == 0 ||
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeView.this);
                    alertDialog.setTitle("Permission require");
                    alertDialog.setMessage("onRequestPermissionsResult denied").setPositiveButton("OK", null);
                    AlertDialog alert = alertDialog.create();
                    alert.show();
                }
        }
    }


    public void testTelegramConnect(View view){
        homePresenter.testTelegramConnect();
    }

    public void connectTeleGuide(View view){
        Intent intent = new Intent(HomeView.this, GuideActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateChatID(String chat_id) {
        if (!chat_id.isEmpty()) {
            mBinding.edChatId.setText(chat_id);
            mBinding.edlToken.setHelperText(getString(R.string.saved));
            PreferenceHelper.putString(mPreferences, Constants.PREF.TOKEN_PREF, String.valueOf(mBinding.edToken.getText()));
            PreferenceHelper.putString(mPreferences, Constants.PREF.CHAT_ID_PREF, String.valueOf(mBinding.edChatId.getText()));
            PreferenceHelper.putBoolean(mPreferences, Constants.PREF.VALID_TOKEN, true);
        } else {
            mBinding.edChatId.setText("");
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (loading) {
            mBinding.layoutScreen.setClickable(false);
            mBinding.layoutScreen.setAlpha(0.3f);
            mBinding.loading.setVisibility(View.VISIBLE);
            Log.d(Constants.TAG, "1");
        } else {
            mBinding.layoutScreen.setClickable(true);
            mBinding.layoutScreen.setAlpha(1f);
            mBinding.loading.setVisibility(View.GONE);
            Log.d(Constants.TAG, "2");
        }
    }

    @Override
    public void enableTestTelegram(boolean enable) {
        mBinding.swTele.setEnabled(enable);
        mBinding.btnTextToTelegram.setEnabled(enable);
        mBinding.swTele.setChecked(enable);
        mBinding.btnTextToTelegram.setAlpha(enable ? 1f : 0.3f);
    }

    @Override
    public void showPhoneValid(boolean isValid) {
        if (isValid) {
            mBinding.edlPhoneNumber.setErrorEnabled(false);
            mBinding.edlPhoneNumber.setHelperText(getString(R.string.saved));
            PreferenceHelper.putString(mPreferences, Constants.PREF.PHONE_NUMBER_PREF, String.valueOf(mBinding.edPhoneNumber.getText()));
        } else {
            mBinding.edlPhoneNumber.setErrorEnabled(true);
            mBinding.edlPhoneNumber.setError(getString(R.string.invalid_phone_number_error));
        }
    }

    @Override
    public void showError(String content) {
        if (content != null && !content.isEmpty()) {
            mBinding.edlToken.setErrorEnabled(true);
            mBinding.edlToken.setError(content);
        } else {
            mBinding.edlToken.setErrorEnabled(false);
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
//                alertDialog.setTitle("AlertDialog");
                String[] items = {getString(R.string.only_send_if_telegram_not_available), getString(R.string.always_send_to_phone)};
                alertDialog.setSingleChoiceItems(items,  mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0), (dialog, option1) -> {
                    int current_option = mPreferences.getInt(Constants.PREF.PHONE_OPTION, 0);
                    if (option1 != current_option) {
                        PreferenceHelper.putInt(mPreferences, Constants.PREF.PHONE_OPTION, option1);
                        Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();
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
}

