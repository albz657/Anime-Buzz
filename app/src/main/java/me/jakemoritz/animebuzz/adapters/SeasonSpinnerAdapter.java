package me.jakemoritz.animebuzz.adapters;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.fragments.SeasonsFragment;
import me.jakemoritz.animebuzz.misc.App;

public class SeasonSpinnerAdapter extends BaseAdapter {

    private List<String> seasonNames = new ArrayList<>();
    private SeasonsFragment fragment;

    public SeasonSpinnerAdapter(SeasonsFragment fragment) {
        this.seasonNames = new ArrayList<>();
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return seasonNames.size();
    }

    @Override
    public String getItem(int position) {
        return seasonNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
            convertView = LayoutInflater.from(App.getInstance()).inflate(R.layout.spinner_seasons_item, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_item);
        textView.setText(seasonNames.get(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")) {
            convertView = LayoutInflater.from(App.getInstance()).inflate(R.layout.spinner_seasons_item_dropdown, parent, false);
            convertView.setTag("DROPDOWN");
        }
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_item_dropdown);
        textView.setText(seasonNames.get(position));

        if (seasonNames.get(position).equals(fragment.getCurrentlyBrowsingSeason().getName())){
            textView.setBackgroundColor(ContextCompat.getColor(App.getInstance(), R.color.season_spinner_active));
        } else {
            textView.setBackgroundResource(0);
        }

        return convertView;
    }

    public List<String> getSeasonNames() {
        return seasonNames;
    }
}
