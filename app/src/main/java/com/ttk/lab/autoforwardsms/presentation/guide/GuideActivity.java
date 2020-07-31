package com.ttk.lab.autoforwardsms.presentation.guide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.ttk.lab.autoforwardsms.R;
import com.ttk.lab.autoforwardsms.databinding.ActivityGuideBinding;

public class GuideActivity extends AppCompatActivity {

    ActivityGuideBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_guide);
    }
}