package me.jakemoritz.animebuzz.api.mal.models;

import android.graphics.Bitmap;

import me.jakemoritz.animebuzz.models.Series;

public class MALImageRequest {

    private Series series;
    private String URL;
    private Bitmap bitmap;

    public MALImageRequest(Series series) {
        this.series = series;
        this.URL = "";
        this.bitmap = null;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
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
}
