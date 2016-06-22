package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Series implements Serializable{
    public String getTitle() {
        return title;
    }

    private String title;

    public int getMal_id() {
        return mal_id;
    }

    private int mal_id;

    public Series(String title, int mal_id){
        this.title = title;
        this.mal_id = mal_id;
    }
}
