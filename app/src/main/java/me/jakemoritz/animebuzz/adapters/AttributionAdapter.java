package me.jakemoritz.animebuzz.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.activities.MainActivity;
import me.jakemoritz.animebuzz.dialogs.TextFileDialogFragment;

public class AttributionAdapter extends RecyclerView.Adapter<AttributionAdapter.ViewHolder> {

    private final static String APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";

    private MainActivity mainActivity;
    private String[][] data;

    public AttributionAdapter(MainActivity activity) {
        this.mainActivity = activity;
        this.data = new String[3][3];

        this.data[0] = activity.getResources().getStringArray(R.array.civ_attrib);
        this.data[1] = activity.getResources().getStringArray(R.array.civ_attrib);
        this.data[2] = activity.getResources().getStringArray(R.array.civ_attrib);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainActivity.getApplicationContext()).inflate(R.layout.attribution_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String[] attributionData = data[position];

        holder.attributionTitle.setText(attributionData[0]);
        holder.attributionLicenseType.setText(attributionData[2]);

        holder.attributionWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri websiteUri = Uri.parse(attributionData[1]);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, websiteUri);
                if (websiteIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
                    mainActivity.startActivity(websiteIntent);
                }
            }
        });

        holder.attributionLicenseWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri licenseUri = Uri.parse(APACHE_LICENSE_URL);
                Intent licenseIntent = new Intent(Intent.ACTION_VIEW, licenseUri);
                if (licenseIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
                    mainActivity.startActivity(licenseIntent);
                }
            }
        });

        holder.attributionLicenseLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextFileDialogFragment textFileDialogFragment = TextFileDialogFragment.newInstance(attributionData[0]);
                textFileDialogFragment.show(mainActivity.getFragmentManager(), TextFileDialogFragment.class.getSimpleName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;

        final TextView attributionTitle;
        final TextView attributionLicenseType;
        final TextView attributionWebsite;
        final TextView attributionLicenseLocal;
        final TextView attributionLicenseWeb;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            attributionTitle = (TextView) itemView.findViewById(R.id.attribution_title);
            attributionLicenseType = (TextView) itemView.findViewById(R.id.attribution_license_type);
            attributionWebsite = (TextView) itemView.findViewById(R.id.attribution_website);
            attributionLicenseLocal = (TextView) itemView.findViewById(R.id.attribution_license_local);
            attributionLicenseWeb = (TextView) itemView.findViewById(R.id.attribution_license_web);
        }
    }
}
