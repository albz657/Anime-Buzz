package me.jakemoritz.animebuzz.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.api.mal.MalApiClient;
import me.jakemoritz.animebuzz.constants;
import me.jakemoritz.animebuzz.dialogs.SimpleDialogFragment;
import me.jakemoritz.animebuzz.misc.App;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;
import me.jakemoritz.animebuzz.utils.SnackbarHelper;

public class ExportFragment extends Fragment implements MainActivity.OrientationChangedListener{

    private final static String TAG = ExportFragment.class.getSimpleName();

    private MainActivity mainActivity;
    private MalApiClient malApiClient;
    private ImageView checkmark;
    private ImageView errorImage;
    private Button exportButton;
    private CircularProgressView progressView;
    private boolean exporting;
    private String status = "";
    private boolean errorDisplayed = false;

    public static ExportFragment newInstance() {
        ExportFragment exportFragment = new ExportFragment();
        exportFragment.setRetainInstance(true);
        exportFragment.malApiClient = new MalApiClient(exportFragment);
        return exportFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.resetToolbar(this);

        if (status.isEmpty()){
            setProgressVisibility("start");
        } else {
            setProgressVisibility(status);
        }
    }

    public void setProgressVisibility(String status){
        this.status = status;

        switch (status){
            case "start":
                exportButton.setVisibility(View.VISIBLE);
                checkmark.setVisibility(View.GONE);
                errorImage.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);
                break;
            case "success":
                exportButton.setVisibility(View.GONE);
                checkmark.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);

                break;
            case "error":
                exportButton.setVisibility(View.GONE);
                checkmark.setVisibility(View.GONE);
                errorImage.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.GONE);

                if (!errorDisplayed) {
                    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(R.string.export_error);
                    dialogFragment.show(getActivity().getFragmentManager(), TAG);

                    errorDisplayed = true;
                }
                break;
            case "inprogress":
                exportButton.setVisibility(View.GONE);
                checkmark.setVisibility(View.GONE);
                errorImage.setVisibility(View.GONE);
                progressView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void orientationChanged(boolean portrait) {
        if (!status.isEmpty()){
            setProgressVisibility(status);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export, container, false);
        mainActivity.getBottomBar().setVisibility(View.GONE);

        exportButton = (Button) view.findViewById(R.id.export_button);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPrefsUtils.getInstance().isLoggedIn()){
                    if (checkExternalPermissions()) {
                        if (App.getInstance().isExternalStorageWritable()) {
                            malApiClient.getUserXml();
                        } else {
                            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(R.string.dialog_no_external);
                            dialogFragment.show(getActivity().getFragmentManager(), TAG);
                        }
                    }
                } else {
                    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(R.string.export_not_logged_in);
                    dialogFragment.show(getActivity().getFragmentManager(), TAG);
                }

            }
        });

        checkmark = (ImageView) view.findViewById(R.id.export_checkmark);
        errorImage = (ImageView) view.findViewById(R.id.export_error);
        progressView = (CircularProgressView) view.findViewById(R.id.progress_view_export);

        return view;
    }

    private boolean checkExternalPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(App.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, constants.WRITE_EXTERNAL_STORAGE_REQUEST);
            return false;
        } else {
            return true;
        }
    }

    public void fixCompatibility(String xml) {
        if (xml != null) {
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

            while (modifiedUserXmlWithoutAnime.contains("<anime>")) {
                modifiedUserXmlWithoutAnime = removeTag(modifiedUserXmlWithoutAnime, "<anime>");
            }

            Pattern pattern = Pattern.compile("\\<anime\\>(.+?)\\<\\/anime\\>");
            Matcher matcher = pattern.matcher(modifiedUserXml);

            while (matcher.find() && !matcher.hitEnd()) {
                String animeEntryXml = matcher.group();

                animeEntryXml = removeTag(animeEntryXml, "<series_synonyms>");
                animeEntryXml = removeTag(animeEntryXml, "<series_status>");
                animeEntryXml = removeTag(animeEntryXml, "<series_start>");
                animeEntryXml = removeTag(animeEntryXml, "<series_end>");
                animeEntryXml = removeTag(animeEntryXml, "<series_image>");

                int seriesType = getIntFromTag(animeEntryXml, "<series_type>");
                String seriesTypeString = "";
                switch (seriesType) {
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
                switch (myStatus) {
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

            saveXmlToStorage(modifiedUserXml);
        } else {
            SnackbarHelper.getInstance().makeSnackbar(getView(), R.string.snackbar_export_fail);
        }

    }

    private File createExportDirectory() {
        File externalPublicDocuments = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (externalPublicDocuments != null) {
            String externalAppStoragePath = externalPublicDocuments.getPath().concat(File.separator).concat("Anime Buzz - MAL Exports");

            File exportDirectory = new File(externalAppStoragePath);
            if (!exportDirectory.exists()) {
                if (exportDirectory.mkdir()) {
                    return exportDirectory;
                }
            } else {
                return exportDirectory;
            }
        } else {
            return null;
        }

        return null;
    }

    private void saveXmlToStorage(String xml) {
        File backupDirectory = createExportDirectory();

        if (backupDirectory != null) {
            String backupFileName = "animebuzz_mal_export_";

            Calendar calendar = Calendar.getInstance();
            String month = new DateFormatSymbols().getShortMonths()[calendar.get(Calendar.MONTH) - 1];
            month = month.toLowerCase();

            SimpleDateFormat timeFormat = new SimpleDateFormat("k:m:s");
            String dateString = timeFormat.format(calendar.getTime());
            backupFileName = backupFileName.concat(month).concat("-").concat(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))).concat("_").concat(dateString);

            String backupFilePath = backupDirectory.getPath().concat(File.separator).concat(backupFileName).concat(".xml");
            try {
                File backupFile = new File(backupFilePath);

                if (backupFile.exists()) {
                    SimpleDateFormat timeFormatMS = new SimpleDateFormat("S");

                    String ms = timeFormatMS.format(calendar.getTime());
                    backupFileName = backupFileName.concat(":").concat(ms);

                    backupFilePath = backupDirectory.getPath().concat(File.separator).concat(backupFileName).concat(".xml");

                    backupFile = new File(backupFilePath);
                }

                FileOutputStream fileOutputStream = new FileOutputStream(backupFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

                outputStreamWriter.write(xml);

                outputStreamWriter.flush();
                outputStreamWriter.close();

                setProgressVisibility("success");
                SnackbarHelper.getInstance().makeSnackbar(getView(), R.string.snackbar_export_success);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(R.string.dialog_no_external);
            dialogFragment.show(getActivity().getFragmentManager(), TAG);
        }
    }

    private int getIntFromTag(String xml, String openingTag) {
        int value = -1;

        int count = 0;
        for (String s : xml.split(openingTag)) {
            if (count == 1) {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(s);

                if (matcher.find()){
                    value = Integer.parseInt(matcher.group());
                }
            }
            count++;
        }

        return value;
    }

    private String removeTag(String xml, String openingTag) {
        String xmlWithoutTag1 = "";
        for (String s : xml.split(openingTag)) {
            if (xmlWithoutTag1.isEmpty()) {
                xmlWithoutTag1 = s;
            }
        }

        String closingTag = openingTag.substring(0, 1) + "/" + openingTag.substring(1, openingTag.length());
        String xmlWithoutTag2 = "";
        int count = 0;
        for (String s : xml.split(closingTag)) {
            if (count == 1) {
                xmlWithoutTag2 = s;
            }
            count++;
        }

        return xmlWithoutTag1.concat(xmlWithoutTag2);
    }

    private String addTag(String xml, String precedingClosingTag, String openingTag, int value) {
        String closingTag = openingTag.substring(0, 1) + "/" + openingTag.substring(1, openingTag.length());

        String tag = openingTag.concat(String.valueOf(value)).concat(closingTag);

        String newXml = "";
        for (String s : xml.split(precedingClosingTag)) {
            if (newXml.isEmpty()) {
                newXml = s;
            } else {
                newXml = newXml.concat(precedingClosingTag).concat(tag).concat(s);
            }
        }

        return newXml;
    }

    private String replaceValue(String xml, String openingTag, String value) {
        String newXml = "";
        for (String s : xml.split(openingTag)) {
            if (newXml.isEmpty()) {
                newXml = s;
            } else {
                newXml = newXml.concat(openingTag).concat(value).concat(s.substring(1, s.length()));
            }
        }

        return newXml;
    }

    public MalApiClient getMalApiClient() {
        if (malApiClient == null) {
            malApiClient = new MalApiClient(this);
        }
        return malApiClient;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        mainActivity.setOrientationChangedListener(this);
    }

    public boolean isExporting() {
        return exporting;
    }

    public void setExporting(boolean exporting) {
        this.exporting = exporting;
    }
}
