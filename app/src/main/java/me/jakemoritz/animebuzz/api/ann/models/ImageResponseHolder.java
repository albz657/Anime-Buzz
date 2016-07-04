package me.jakemoritz.animebuzz.api.ann.models;

import android.graphics.Bitmap;

public class ImageResponseHolder {

    private String ANNID;
    private String size;
    private Bitmap bitmap;

    public ImageResponseHolder(String ANNID, String size, Bitmap bitmap) {
        this.ANNID = ANNID;
        this.size = size;
        this.bitmap = bitmap;
    }

    public String getANNID() {
        return ANNID;
    }

    public String getSize() {
        return size;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
