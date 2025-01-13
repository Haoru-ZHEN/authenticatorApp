package com.example.fyp_authenticator.Model;

public class ApplicationModel {
    String webEmail,UIDweb,userInfo,userID,type,idKey,appName,connectKey,SHAalgo;
    long counter;

    public ApplicationModel() {
    }

    public ApplicationModel(String webEmail, String UIDweb, String userInfo, String userID, String type, String idkey, String appName, String connectKey, String SHAalgo, long counter) {
        this.webEmail = webEmail;
        this.UIDweb = UIDweb;
        this.userInfo = userInfo;
        this.userID = userID;
        this.type = type;
        this.idKey = idKey
        ;
        this.appName = appName;
        this.connectKey = connectKey;
        this.SHAalgo = SHAalgo;
        this.counter = counter;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public String getSHAalgo() {
        return SHAalgo;
    }

    public void setSHAalgo(String SHAalgo) {
        this.SHAalgo = SHAalgo;
    }

    public String getConnectKey() {
        return connectKey;
    }

    public void setConnectKey(String connectKey) {
        this.connectKey = connectKey;
    }

    public String getWebEmail() {
        return webEmail;
    }

    public void setWebEmail(String webEmail) {
        this.webEmail = webEmail;
    }

    public String getUIDweb() {
        return UIDweb;
    }

    public void setUIDweb(String UIDweb) {
        this.UIDweb = UIDweb;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdKey() {
        return idKey;
    }

    public void setIdKey(String idKey) {
        this.idKey = idKey;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
