package me.jakemoritz.animebuzz.helpers;

import android.app.Application;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
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

        loadFromDb();

        mInstance = this;
    }

    public static synchronized App getInstance(){
        return mInstance;
    }

    private void loadFromDb(){
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.onCreate(dbHelper.getWritableDatabase());
        userList = dbHelper.getSeriesFromDb(getString(R.string.table_user_list));
        seasonData = dbHelper.getSeriesFromDb(getString(R.string.table_seasons));
    }
}
