package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;

public class ChangelogDialogFragment extends DialogFragment {

    public ChangelogDialogFragment() {
    }

    public static ChangelogDialogFragment newInstance() {
        ChangelogDialogFragment fragment = new ChangelogDialogFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View titleView = getActivity().getLayoutInflater().inflate(R.layout.dialog_changelog_title, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("What's new?")
                .setCustomTitle(titleView)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_changelog, null);

        RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.changelog_recycler);

        ChangelogAdapter changelogAdapter = new ChangelogAdapter(loadChangelog());
        recyclerView.setAdapter(changelogAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        builder.setView(dialogView);
        return builder.create();
    }

    private class ChangelogComparator implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {
            String[] leftArray = o1.split("\\.");
            String[] rightArray = o2.split("\\.");

            Integer majorLeft = Integer.valueOf(leftArray[0]);
            Integer majorRight = Integer.valueOf(rightArray[0]);
            if (majorLeft.equals(majorRight)){
                Integer minorLeft = Integer.valueOf(leftArray[1]);
                Integer minorRight = Integer.valueOf(rightArray[1]);

                if (minorLeft.equals(minorRight)){
                    Integer patchLeft = Integer.valueOf(leftArray[2]);
                    Integer patchRight = Integer.valueOf(rightArray[2]);

                    return patchLeft.compareTo(patchRight);
                } else {
                    return minorLeft.compareTo(minorRight);
                }
            } else {
                return majorLeft.compareTo(majorRight);
            }
        }
    }

    private ArrayList<ChangelogItem> loadChangelog(){
        ArrayList<ChangelogItem> changelogItems = new ArrayList<>();

        try {
            String[] changelogs = App.getInstance().getResources().getAssets().list("changelogs");
            ArrayList<String> changelogList = new ArrayList<>(Arrays.asList(changelogs));
            Collections.sort(changelogList, new ChangelogComparator());
            Collections.reverse(changelogList);

            for (String changelogFilename : changelogList){
                String changelogVersion = changelogFilename.replace(".txt", "");

                InputStream inputStream = App.getInstance().getResources().getAssets().open("changelogs".concat(File.separator).concat(changelogFilename));

                String newChanges = "";
                String fixes = "";

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder text = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null){
                    if (line.trim().matches("Fixes")){
                        newChanges = text.toString();
                        text = new StringBuilder();
                    } else if (!line.trim().matches("New")){
                        text.append(line).append("\n");
                    }
                }

                bufferedReader.close();
                inputStream.close();

                fixes = text.toString();

                ChangelogItem changelogItem = new ChangelogItem(changelogVersion.trim(), newChanges.trim(), fixes.trim());
                changelogItems.add(changelogItem);
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        return changelogItems;
    }

    private class ChangelogItem {
        private String versionName;
        private String changelogNew;
        private String changelogFixes;

        public ChangelogItem(String versionName, String changelogNew, String changelogFixes) {
            this.versionName = versionName;
            this.changelogNew = changelogNew;
            this.changelogFixes = changelogFixes;
        }

        public String getChangelogFixes() {
            return changelogFixes;
        }

        public String getChangelogNew() {
            return changelogNew;
        }

        public String getVersionName() {
            return versionName;
        }
    }

    private class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

        private ArrayList<ChangelogItem> changelog;

        public ChangelogAdapter(ArrayList<ChangelogItem> changelog) {
            this.changelog = changelog;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.changelog_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.changelogItem = changelog.get(position);

            holder.mTitle.setText(holder.changelogItem.getVersionName());

            if (!holder.changelogItem.getChangelogNew().isEmpty()){
                TextView textView = new TextView(getActivity());

                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        r.getDisplayMetrics()
                );

                textView.setPadding(px, 0, 0, 0);

                textView.setText(holder.changelogItem.getChangelogNew());

                LinearLayout linearLayout = (LinearLayout) holder.mView.findViewById(R.id.changelog_content);
                int indexOfNew = linearLayout.indexOfChild(holder.mNewChanges);
                linearLayout.addView(textView, indexOfNew + 1);
            } else {
                holder.mNewChanges.setVisibility(View.GONE);
            }

            if (!holder.changelogItem.getChangelogFixes().isEmpty()){
                TextView textView = new TextView(getActivity());

                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        r.getDisplayMetrics()
                );

                textView.setPadding(px, 0, 0, 0);

                textView.setText(holder.changelogItem.getChangelogFixes());

                LinearLayout linearLayout = (LinearLayout) holder.mView.findViewById(R.id.changelog_content);
                int indexOfFixes = linearLayout.indexOfChild(holder.mFixes);
                linearLayout.addView(textView, indexOfFixes + 1);

                if (holder.mNewChanges.getVisibility() == View.VISIBLE){
                    int topPx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            5,
                            r.getDisplayMetrics()
                    );

                    holder.mFixes.setPadding(0, topPx, 0, 0);
                }
            } else {
                holder.mFixes.setVisibility(View.GONE);
            }

            if (position == 0){
                holder.mNewIcon.setVisibility(View.VISIBLE);
                int color = ContextCompat.getColor(getActivity(), R.color.colorAccent);
                ColorFilter colorFilter = new LightingColorFilter(color, color);
                holder.mNewIcon.setColorFilter(colorFilter);
            }
        }

        @Override
        public int getItemCount() {
            return changelog.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;

            final TextView mTitle;
            final TextView mNewChanges;
            final TextView mFixes;
            final ImageView mNewIcon;

            ChangelogItem changelogItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mTitle = (TextView) view.findViewById(R.id.changelog_version);
                mNewChanges = (TextView) view.findViewById(R.id.changelog_text_new);
                mFixes = (TextView) view.findViewById(R.id.changelog_text_fixes);
                mNewIcon = (ImageView) view.findViewById(R.id.changelog_new_icon);
            }
        }
    }


}
