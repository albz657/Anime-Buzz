package me.jakemoritz.animebuzz.api.mal.models;

import android.graphics.Bitmap;

public class MALImageRequest {

    private String MALID;
    private String URL;
    private Bitmap bitmap;

    public MALImageRequest(String MALID) {
        this.MALID = MALID;
        this.URL = "";
        this.bitmap = null;
    }

    public String getMALID() {
        return MALID;
    }

    public void setMALID(String MALID) {
        this.MALID = MALID;
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
