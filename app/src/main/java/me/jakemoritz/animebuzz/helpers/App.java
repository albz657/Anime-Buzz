package me.jakemoritz.animebuzz.helpers;

import android.app.Application;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.Series;

public class App extends Application {
    private static App mInstance;

    public ArrayList<Series> getSeasonData() {
        return seasonData;
    }

    public void setSeasonData(ArrayList<Series> seasonData) {
        this.seasonData = seasonData;
    }

    public ArrayList<Series> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<Series> userList) {
        this.userList = userList;
    }

    private ArrayList<Series> userList;
    private ArrayList<Series> seasonData;

    @Override
    public void onCreate() {
        super.onCreate();

        userList = new ArrayList<>();
        seasonData = new ArrayList<>();
        mInstance = this;
    }

    public static synchronized App getInstance(){
        return mInstance;
    }
}
