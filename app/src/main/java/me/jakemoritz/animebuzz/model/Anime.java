package me.jakemoritz.animebuzz.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Anime extends RealmObject {

    @PrimaryKey
    @Required
    private String malId;

    private String title;

    private String type;

    private int episodes;

    private String englishTitle;

    private String synopsis;

    private String malLink;

    public Anime() {
    }

    public Anime(String malId, String title) {
        this.malId = malId;
        this.title = title;
    }

    public Anime(SenpaiAnime senpaiAnime, JikanAnime jikanAnime) {
        this.malId = senpaiAnime.getMalId();
        this.title = jikanAnime.getTitle();
        this.englishTitle = jikanAnime.getEnglishTitle();
        this.type = jikanAnime.getType();
        this.synopsis = jikanAnime.getSynopsis();
        this.malLink = jikanAnime.getMalLink();
        this.episodes = jikanAnime.getEpisodes();
    }

    public String getMalIdFromMalLink(){
        Pattern pattern = Pattern.compile("anime\\\\/(\\d*)\\\\");
        Matcher matcher = pattern.matcher(this.malId);
        return matcher.matches() ? matcher.group() : "";
    }

    public String getMalId() {
        return malId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public int getEpisodes() {
        return episodes;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getMalLink() {
        return malLink;
    }
}
