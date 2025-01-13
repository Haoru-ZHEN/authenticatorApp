package com.example.fyp_authenticator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.fyp_authenticator.Utilities.SessionManager;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    ImageView backIcon;
    Switch switch2;
    String sessionEMAIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        backIcon = findViewById(R.id.backIcon_setting);
        switch2 = findViewById(R.id.switch2_setting);

        //default
        SessionManager sm2 = new SessionManager(getApplicationContext(), SessionManager.SESSION_KEEPLOGGED);
        HashMap<String, String> keepLogDetails = sm2.getData();
        sessionEMAIL = keepLogDetails.get(SessionManager.KEY_EMAIL);

        loadSettings();


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SessionManager sm = new SessionManager(getApplicationContext(), SessionManager.SESSION_BIOMETRIC);

                if (sm.checkIsSetupBA()) {
                    sm.insertBAEmail(sessionEMAIL);
                    sm.insertBioData(switch2.isChecked());
                } else {
                    Toast.makeText(getApplicationContext(), "You haven't setup any biometric authentication yet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSettings() {
        SessionManager sm = new SessionManager(getApplicationContext(), SessionManager.SESSION_BIOMETRIC);
        //switch1.setChecked(sm.checkIsHideOTP());
        if (sm.getBAEmail().equals(sessionEMAIL)) {
            if (!sm.checkIsSetupBA()) {
                switch2.setEnabled(false);
            } else {
                switch2.setChecked(sm.checkIsBiometricEnabled());

            }
        }
    }
}