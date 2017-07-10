package me.jakemoritz.animebuzz.adapters;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.dialogs.TextFileDialogFragment;
import me.jakemoritz.animebuzz.fragments.AttributionFragment;
import me.jakemoritz.animebuzz.misc.App;

public class AttributionAdapter extends RecyclerView.Adapter<AttributionAdapter.ViewHolder> {

    private final static String APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";
    private final static String BSD_2_CLAUSE_LICENSE_URL = "https://opensource.org/licenses/BSD-2-Clause";
    private final static String BSD_3_CLAUSE_LICENSE_URL = "https://opensource.org/licenses/BSD-3-Clause";

    private AttributionFragment attributionFragment;
    private List<String[]> data;

    public AttributionAdapter(AttributionFragment attributionFragment) {
        this.attributionFragment = attributionFragment;
        this.data = buildDataArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(attributionFragment.getContext()).inflate(R.layout.attribution_list_item, parent, false);
        return new ViewHolder(view);
    }

    private List<String[]> buildDataArray(){
        List<String[]> data = new ArrayList<>();
        XmlResourceParser parser = App.getInstance().getResources().getXml(R.xml.attributions);

        try {
            int eventType = parser.getEventType();
            int itemIndex = 0;
            while (eventType != XmlPullParser.END_DOCUMENT){
                if (eventType == XmlPullParser.START_TAG){
                    if (parser.getName().equals("string-array")){
                        String[] attributionItem = new String[4];
                        attributionItem[3] = parser.getAttributeValue(0);
                        data.add(attributionItem);
                        itemIndex = 0;
                    }
                } else if (eventType == XmlPullParser.TEXT){
                    String itemText = parser.getText();
                    if (itemText.contains("@string")){
                        String resName = itemText.replace("@string/", "");
                        int resId = App.getInstance().getResources().getIdentifier(resName, "string", App.getInstance().getPackageName());
                        itemText = App.getInstance().getString(resId);
                    }
                    data.get(data.size() - 1)[itemIndex++] = itemText;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String[] attributionData = data.get(position);

        holder.attributionTitle.setText(attributionData[0]);

        holder.attributionWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri websiteUri = Uri.parse(attributionData[1]);
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, websiteUri);
                if (websiteIntent.resolveActivity(attributionFragment.getActivity().getPackageManager()) != null) {
                    attributionFragment.getActivity().startActivity(websiteIntent);
                }
            }
        });

        holder.attributionLicenseType.setText(attributionData[2]);

        if (!attributionData[2].equals("none") && !attributionData[2].equals("API")){
            holder.attributionLicenseWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri licenseUri = null;

                    if (attributionData[2].equals(App.getInstance().getString(R.string.apache_2_0))){
                        licenseUri = Uri.parse(APACHE_LICENSE_URL);
                    } else if (attributionData[2].equals(App.getInstance().getString(R.string.bsd_2_clause))){
                        licenseUri = Uri.parse(BSD_2_CLAUSE_LICENSE_URL);
                    } else if (attributionData[2].equals(App.getInstance().getString(R.string.bsd_3_clause))){
                        licenseUri = Uri.parse(BSD_3_CLAUSE_LICENSE_URL);
                    }
                    Intent licenseIntent = new Intent(Intent.ACTION_VIEW, licenseUri);
                    if (licenseIntent.resolveActivity(attributionFragment.getActivity().getPackageManager()) != null) {
                        attributionFragment.getActivity().startActivity(licenseIntent);
                    }
                }
            });

            holder.attributionLicenseLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextFileDialogFragment textFileDialogFragment = TextFileDialogFragment.newInstance(attributionData[3]);
                    textFileDialogFragment.show(attributionFragment.getActivity().getFragmentManager(), TextFileDialogFragment.class.getSimpleName());
                }
            });
        } else {
            if (!attributionData[2].equals("API")){
                holder.attributionLicenseType.setVisibility(View.INVISIBLE);
            }

            holder.attributionLicenseType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            holder.attributionLicenseLocal.setVisibility(View.INVISIBLE);
            holder.attributionLicenseWeb.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
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
