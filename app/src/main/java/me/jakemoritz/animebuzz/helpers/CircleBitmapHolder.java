package me.jakemoritz.animebuzz.helpers;

import android.graphics.Bitmap;

import java.io.File;

public class CircleBitmapHolder {

    String ANNID;
    Bitmap bitmap;
    File file;

    public CircleBitmapHolder(String ANNID, Bitmap bitmap, File file) {
        this.ANNID = ANNID;
        this.bitmap = bitmap;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getANNID() {
        return ANNID;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
