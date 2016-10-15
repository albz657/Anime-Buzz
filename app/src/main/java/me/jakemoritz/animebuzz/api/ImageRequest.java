package me.jakemoritz.animebuzz.api;

import android.graphics.Bitmap;

import me.jakemoritz.animebuzz.models.Series;

public class ImageRequest {

    private String MALID;
    private String URL;
    private Bitmap bitmap;

    public ImageRequest(Series series) {
        this.MALID = series.getMALID();
        this.URL = "";
        this.bitmap = null;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getMALID() {
        return MALID;
    }
}
