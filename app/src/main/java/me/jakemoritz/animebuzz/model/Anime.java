package me.jakemoritz.animebuzz.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Anime extends RealmObject{

    @PrimaryKey
    @Required
    private String malId;

    private String name;

    public Anime() {
    }

    public Anime(String malId, String name) {
        this.malId = malId;
        this.name = name;
    }

    public String getMalId() {
        return malId;
    }

    public void setMalId(String malId) {
        this.malId = malId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
