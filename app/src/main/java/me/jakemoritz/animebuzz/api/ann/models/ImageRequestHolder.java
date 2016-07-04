package me.jakemoritz.animebuzz.api.ann.models;

public class ImageRequestHolder {

    private String URL;
    private String ANNID;
    private String size;

    public ImageRequestHolder(String URL, String ANNID, String size) {
        this.URL = URL;
        this.ANNID = ANNID;
        this.size = size;
    }

    public String getURL() {
        return URL;
    }

    public String getANNID() {
        return ANNID;
    }

    public String getSize() {
        return size;
    }
}
