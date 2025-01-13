package com.example.fyp_authenticator.Model;

public class LogModel {
    String idKey,idLog,OTP,appName;
    long timestamp;

    public LogModel() {
    }

    public LogModel(String idKey, String idLog, String OTP, String appName, long timestamp) {
        this.idKey = idKey;
        this.idLog = idLog;
        this.OTP = OTP;
        this.appName = appName;
        this.timestamp = timestamp;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getIdLog() {
        return idLog;
    }

    public void setIdLog(String idLog) {
        this.idLog = idLog;
    }

    public String getOTP() {
        return OTP;
    }

    public void setOTP(String OTP) {
        this.OTP = OTP;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
