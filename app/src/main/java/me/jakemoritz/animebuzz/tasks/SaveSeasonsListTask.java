package me.jakemoritz.animebuzz.tasks;

import android.os.AsyncTask;

import java.util.Set;

import me.jakemoritz.animebuzz.models.SeasonMetadata;

public class SaveSeasonsListTask extends AsyncTask<Set<SeasonMetadata>, Void, Void> {


    @Override
    protected Void doInBackground(Set<SeasonMetadata>... sets) {
        SeasonMetadata.saveInTx(sets[0]);
        return null;
    }
}
