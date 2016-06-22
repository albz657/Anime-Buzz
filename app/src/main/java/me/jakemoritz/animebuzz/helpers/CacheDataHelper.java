package me.jakemoritz.animebuzz.helpers;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.Series;

public class CacheDataHelper {

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    private Activity mActivity;

    public static CacheDataHelper newInstance(Activity mActivity){
        CacheDataHelper helper = new CacheDataHelper();
        helper.setmActivity(mActivity);
        return helper;
    }

    public void cacheSeasonData(ArrayList<Series> seriesList, String filename){
        try {
            FileOutputStream fos = mActivity.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(seriesList);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Series> readCache(String filename){
        try {
            FileInputStream fis = mActivity.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Series> readData = new ArrayList<>();
            readData = (ArrayList<Series>) ois.readObject();
            return readData;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
