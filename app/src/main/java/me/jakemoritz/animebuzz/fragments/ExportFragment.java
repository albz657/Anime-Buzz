package me.jakemoritz.animebuzz.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.helpers.App;
import me.jakemoritz.animebuzz.helpers.SnackbarHelper;

public class ExportFragment extends Fragment {

    private final static String TAG = ExportFragment.class.getSimpleName();

    private MainActivity mainActivity;
    private MalApiClient malApiClient;

    public static ExportFragment newInstance() {
        ExportFragment exportFragment = new ExportFragment();
        exportFragment.setRetainInstance(true);
        exportFragment.malApiClient = new MalApiClient(exportFragment);
        return exportFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.fixToolbar(this.getClass().getSimpleName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export, container, false);
        mainActivity.getBottomBar().setVisibility(View.GONE);

        Button exportButton = (Button) view.findViewById(R.id.export_button);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalPermissions()){
                    malApiClient.getUserXml();
                }
            }
        });
        return view;
    }

    private boolean checkExternalPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(App.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, constants.WRITE_EXTERNAL_STORAGE_REQUEST);
            return false;
        } else {
            return true;
        }
    }

    public void fixCompatibility(String xml){
        if (xml != null){
            String modifiedUserXml = addTag(xml, "</user_name>", "<user_export_type>", 1);

            int userWatching = getIntFromTag(modifiedUserXml, "<user_watching>");
            int userCompleted = getIntFromTag(modifiedUserXml, "<user_completed>");
            int userOnHold = getIntFromTag(modifiedUserXml, "<user_onhold>");
            int userDropped = getIntFromTag(modifiedUserXml, "<user_dropped>");
            int userPlanToWatch = getIntFromTag(modifiedUserXml, "<user_plantowatch>");

            int userTotalAnime = userWatching + userCompleted + userOnHold + userDropped + userPlanToWatch;

            modifiedUserXml = modifiedUserXml.replace("user_watching", "user_total_watching");
            modifiedUserXml = modifiedUserXml.replace("user_completed", "user_total_completed");
            modifiedUserXml = modifiedUserXml.replace("user_onhold", "user_total_onhold");
            modifiedUserXml = modifiedUserXml.replace("user_dropped", "user_total_dropped");
            modifiedUserXml = modifiedUserXml.replace("user_plantowatch", "user_total_plantowatch");

            modifiedUserXml = removeTag(modifiedUserXml, "<user_days_spent_watching>");

            modifiedUserXml = addTag(modifiedUserXml, "</user_export_type>", "<user_total_anime>", userTotalAnime);

            String modifiedUserXmlWithoutAnime = modifiedUserXml;

            while (modifiedUserXmlWithoutAnime.contains("<anime>")){
                modifiedUserXmlWithoutAnime = removeTag(modifiedUserXmlWithoutAnime, "<anime>");
            }

            Pattern pattern = Pattern.compile("\\<anime\\>(.+?)\\<\\/anime\\>");
            Matcher matcher = pattern.matcher(modifiedUserXml);

            while (matcher.find() && !matcher.hitEnd()){
                String animeEntryXml = matcher.group();

                animeEntryXml = removeTag(animeEntryXml, "<series_synonyms>");
                animeEntryXml = removeTag(animeEntryXml, "<series_status>");
                animeEntryXml = removeTag(animeEntryXml, "<series_start>");
                animeEntryXml = removeTag(animeEntryXml, "<series_end>");
                animeEntryXml = removeTag(animeEntryXml, "<series_image>");

                int seriesType = getIntFromTag(animeEntryXml, "<series_type>");
                String seriesTypeString = "";
                switch (seriesType){
                    case 1:
                        seriesTypeString = "TV";
                        break;
                    case 2:
                        seriesTypeString = "OVA";
                        break;
                    case 3:
                        seriesTypeString = "Movie";
                        break;
                    case 6:
                        seriesTypeString = "Music";
                        break;
                    case 4:
                        seriesTypeString = "Special";
                        break;
                    case 5:
                        seriesTypeString = "ONA";
                        break;
                }

                animeEntryXml = replaceValue(animeEntryXml, "<series_type>", seriesTypeString);

                int myStatus = getIntFromTag(animeEntryXml, "<my_status>");
                String myStatusString = "";
                switch (myStatus){
                    case 1:
                        myStatusString = "Watching";
                        break;
                    case 2:
                        myStatusString = "Completed";
                        break;
                    case 3:
                        myStatusString = "On-Hold";
                        break;
                    case 4:
                        myStatusString = "Dropped";
                        break;
                    case 6:
                        myStatusString = "Plan To Watch";
                        break;
                }

                animeEntryXml = replaceValue(animeEntryXml, "<my_status>", myStatusString);
                animeEntryXml = removeTag(animeEntryXml, "<my_last_updated>");
                animeEntryXml = addTag(animeEntryXml, "</my_rewatching_ep>", "<update_on_import>", 1);
                modifiedUserXmlWithoutAnime = modifiedUserXmlWithoutAnime.concat(animeEntryXml);
            }

            String endingTag = "</myanimelist>";
            modifiedUserXml = modifiedUserXmlWithoutAnime.concat(endingTag);

            Log.d(TAG, "s");
        } else {
            SnackbarHelper.getInstance().makeSnackbar(getView(), R.string.snackbar_export_fail);
        }

    }

    private int getIntFromTag(String xml, String openingTag){
        int value = -1;

        int count = 0;
        for (String s : xml.split(openingTag)){
            if (count == 1){
                value = Integer.parseInt(s.substring(0, 1));
            }
            count++;
        }

        return value;
    }

    private String removeTag(String xml, String openingTag){
        String xmlWithoutTag1 = "";
        for (String s : xml.split(openingTag)){
            if (xmlWithoutTag1.isEmpty()){
                xmlWithoutTag1 = s;
            }
        }

        String closingTag = openingTag.substring(0, 1) + "/" + openingTag.substring(1, openingTag.length());
        String xmlWithoutTag2 = "";
        int count = 0;
        for (String s : xml.split(closingTag)){
            if (count == 1){
                xmlWithoutTag2 = s;
            }
            count++;
        }

        return xmlWithoutTag1.concat(xmlWithoutTag2);
    }

    private String addTag(String xml, String precedingClosingTag, String openingTag, int value){
        String closingTag = openingTag.substring(0, 1) + "/" + openingTag.substring(1, openingTag.length());

        String tag = openingTag.concat(String.valueOf(value)).concat(closingTag);

        String newXml = "";
        for (String s : xml.split(precedingClosingTag)){
            if (newXml.isEmpty()){
                newXml = s;
            } else {
                newXml = newXml.concat(precedingClosingTag).concat(tag).concat(s);
            }
        }

        return newXml;
    }

    private String replaceValue(String xml, String openingTag, String value){
        String newXml = "";
        for (String s : xml.split(openingTag)){
            if (newXml.isEmpty()){
                newXml = s;
            } else {
                newXml = newXml.concat(openingTag).concat(value).concat(s.substring(1, s.length()));
            }
        }

        return newXml;
    }

    public MalApiClient getMalApiClient() {
        if (malApiClient == null){
            malApiClient = new MalApiClient(this);
        }
        return malApiClient;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }
}
