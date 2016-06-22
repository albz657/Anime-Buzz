package me.jakemoritz.animebuzz.models;

import java.io.Serializable;

public class Series implements Serializable{
    public String getTitle() {
        return title;
    }

    private String title;

    public Series(String title){
        this.title = title;
    }
}
