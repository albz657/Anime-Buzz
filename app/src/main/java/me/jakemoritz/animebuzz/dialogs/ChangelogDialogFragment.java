package me.jakemoritz.animebuzz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;

public class ChangelogDialogFragment extends DialogFragment {

    public ChangelogDialogFragment() {
    }

    public static ChangelogDialogFragment newInstance(String assetFileName) {
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

    private ArrayList<ChangelogItem> loadChangelog(){
        ArrayList<ChangelogItem> changelogItems = new ArrayList<>();

        try {
            String[] changelogs = App.getInstance().getResources().getAssets().list("changelogs");
            ArrayList<String> changelogList = new ArrayList<>(Arrays.asList(changelogs));
            Collections.reverse(changelogList);

            for (String changelogFilename : changelogList){
                String changelogVersion = changelogFilename.replace(".txt", "");

                InputStream inputStream = App.getInstance().getResources().getAssets().open("changelogs".concat(File.separator).concat(changelogFilename));


                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder text = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null){
                    text.append(line).append("\n");
                }

                bufferedReader.close();
                inputStream.close();

                String changelogContext = text.toString();

                ChangelogItem changelogItem = new ChangelogItem(changelogVersion.trim(), changelogContext.trim());
                changelogItems.add(changelogItem);
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        return changelogItems;
    }

    private class ChangelogItem {
        private String versionName;
        private String changelogContent;


        public ChangelogItem(String versionName, String changelogContent) {
            this.versionName = versionName;
            this.changelogContent = changelogContent;
        }


        public String getChangelogContent() {
            return changelogContent;
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
            holder.mContent.setText(holder.changelogItem.getChangelogContent());

            if (position == 0){
                holder.mNew.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return changelog.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;

            final TextView mTitle;
            final TextView mContent;
            final ImageView mNew;

            ChangelogItem changelogItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mTitle = (TextView) view.findViewById(R.id.changelog_version);
                mContent = (TextView) view.findViewById(R.id.changelog_text);
                mNew = (ImageView) view.findViewById(R.id.changelog_new_icon);
            }
        }
    }


}
