package com.example.fyp_authenticator.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    //session name
    public static final String SESSION_KEEPLOGGED = "loggedIn";
    public static final String SESSION_BIOMETRIC = "biometric";
    //public static final String SESSION_FTIME = "firstTimeSession";

    //variables
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    private static final String IS_KEEPLOGGED = "isKeepLogged";
    private static final String IS_BIOENABLED = "isBiometricEnabled";
    private static final String IS_HIDEOTP = "isOTPHide";
    private static final String IS_SETUPBA = "isSetupBA";
    public static final String BA_EMAIL = "baEmail";

    public SessionManager(Context context, String sessionName) {
        this.sharedPreferences = context.getSharedPreferences(sessionName, context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.context = context;
    }

    public void clearData() {
        editor.clear();
        editor.commit();
    }

    //For SESSION_KEEPLOGGED
    public void insertData(String EMAIL, String PASSWORD) {
        editor.putBoolean(IS_KEEPLOGGED, true);
        editor.putString(KEY_EMAIL, EMAIL);
        editor.putString(KEY_PASSWORD, PASSWORD);

        //editor commit data
        editor.commit();
    }

    public HashMap<String, String> getData() {
        HashMap<String, String> userData = new HashMap<String, String>();
        userData.put(KEY_EMAIL, sharedPreferences.getString(KEY_EMAIL, ""));
        userData.put(KEY_PASSWORD, sharedPreferences.getString(KEY_PASSWORD, ""));
        return userData;
    }

    public Boolean checkIsLoggedIn() {
        if (sharedPreferences.getBoolean(IS_KEEPLOGGED, false)) {
            return true;
        } else {
            return false;
        }
    }

    //FOR SESSION BIOMETRIC
    public void insertBioData(boolean BOOLEANVALUE) {
        editor.putBoolean(IS_BIOENABLED, BOOLEANVALUE);
        //editor commit data
        editor.commit();
    }
    public void insertHideData(boolean BOOLEANVALUE) {
        editor.putBoolean(IS_HIDEOTP, BOOLEANVALUE);
        //editor commit data
        editor.commit();
    }
    public void insertSetupBAData(boolean BOOLEANVALUE) {
        editor.putBoolean(IS_SETUPBA, BOOLEANVALUE);
        //editor commit data
        editor.commit();
    }
    public void insertBAEmail(String EMAIL) {
        editor.putString(BA_EMAIL, EMAIL);
        //editor commit data
        editor.commit();
    }
    public String getBAEmail() {
        return sharedPreferences.getString(BA_EMAIL, "");
    }
    public Boolean checkIsBiometricEnabled() {
        if (sharedPreferences.getBoolean(IS_BIOENABLED, false)) {
            return true;
        } else {
            return false;
        }
    }
    public Boolean checkIsSetupBA() {
        if (sharedPreferences.getBoolean(IS_SETUPBA, false)) {
            return true;
        } else {
            return false;
        }
    }
    public Boolean checkIsHideOTP() {
        if (sharedPreferences.getBoolean(IS_HIDEOTP, false)) {
            return true;
        } else {
            return false;
        }
    }



}
