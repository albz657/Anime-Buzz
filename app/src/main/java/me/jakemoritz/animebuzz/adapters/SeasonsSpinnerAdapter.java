package me.jakemoritz.animebuzz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;

public class SeasonsSpinnerAdapter extends BaseAdapter {

    public List<String> getSeasons() {
        return seasons;
    }

    private List<String> seasons = new ArrayList<>();
    private Context context;

    public SeasonsSpinnerAdapter(Context context, List<String> seasons) {
        this.seasons = seasons;
        this.context = context;
    }

    @Override
    public int getCount() {
        return seasons.size();
    }

    @Override
    public String getItem(int position) {
        return seasons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")){
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_seasons_item, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_item);
        textView.setText(seasons.get(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")){
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_seasons_item_dropdown, parent, false);
            convertView.setTag("DROPDOWN");
        }
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_item_dropdown);
        textView.setText(seasons.get(position));
        return convertView;    }
}
