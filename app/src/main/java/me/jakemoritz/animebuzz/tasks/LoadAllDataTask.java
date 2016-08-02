package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import me.jakemoritz.animebuzz.data.DatabaseHelper;
import me.jakemoritz.animebuzz.helpers.App;

public class LoadAllDataTask extends AsyncTask<Void, Void, Void> {

    DatabaseHelper databaseHelper;


    @Override
    protected Void doInBackground(Void... voids) {
        databaseHelper = DatabaseHelper.getInstance(App.getInstance());



        return null;
    }


}
