package me.jakemoritz.animebuzz.helpers;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.models.Series;

public class App extends Application {
    private final static String TAG = App.class.getSimpleName();

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
    private HashMap<Series, Intent> alarms;
    private HashMap<Series, IntentWrapper> alarmsSerialized;

    public HashMap<Series, Intent> getAlarms() {
        return alarms;
    }

    public void addAlarm(Series series, Intent intent) {
        this.alarms.put(series, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        userList = new ArrayList<>();
        seasonData = new ArrayList<>();
        alarms = new HashMap<>();

        loadFromDb();
        loadAlarms();

        mInstance = this;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    private void loadFromDb() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.onCreate(dbHelper.getWritableDatabase());
        userList = dbHelper.getSeriesFromDb(getString(R.string.table_user_list));
        seasonData = dbHelper.getSeriesFromDb(getString(R.string.table_seasons));
    }

    private void loadAlarms() {
        try {
            FileInputStream fis = new FileInputStream(getFilesDir().getPath() + "/" + getString(R.string.file_alarms));
            ObjectInputStream ois = new ObjectInputStream(fis);
            HashMap<Series, IntentWrapper> tempAlarms = (HashMap<Series, IntentWrapper>) ois.readObject();
            if (tempAlarms != null) {
                deserializeAlarms(tempAlarms);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // user has no alarms
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveAlarms() {
        serializeAlarms();
    }

    private void deserializeAlarms(HashMap<Series, IntentWrapper> serializedAlarms) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long numTime = 1466645400000L;

        HashMap<Series, Intent> tempAlarms = new HashMap<>();
        Set<Series> set = serializedAlarms.keySet();
        List<Series> list = new ArrayList<>();
        list.addAll(set);
        while (!serializedAlarms.isEmpty()) {
            Series tempSeries = list.remove(0);
            IntentWrapper wrapper = serializedAlarms.remove(tempSeries);
            Intent tempIntent = new Intent(wrapper.getAction(), wrapper.getUri());
//            PendingIntent tempPendingIntent = PendingIntent.getBroadcast(this, 0, tempIntent, 0);
            tempAlarms.put(tempSeries, tempIntent);
        }
        this.alarms = tempAlarms;
    }

    private void serializeAlarms() {
        HashMap<Series, IntentWrapper> serializedAlarms = new HashMap<>();
        Set<Series> set = alarms.keySet();
        List<Series> list = new ArrayList<>();
        list.addAll(set);
        while (!alarms.isEmpty()) {
            Series tempSeries = list.remove(0);
            Intent tempIntent = alarms.remove(tempSeries);
            IntentWrapper wrapper = new IntentWrapper(tempIntent.getAction(), tempIntent.getData());
            serializedAlarms.put(tempSeries, wrapper);
        }
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.file_alarms), Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serializedAlarms);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
