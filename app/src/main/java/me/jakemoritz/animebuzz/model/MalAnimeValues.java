package me.jakemoritz.animebuzz.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "entry")
public class MalAnimeValues {

    @Element
    private int episode;

    @Element
    private int status;

    @Element
    private int score;

    @Element
    private int storage_type;

    @Element
    private float storage_value;

    @Element
    private int times_rewatched;

    @Element
    private int rewatch_value;

    @Element
    private String date_start;

    @Element
    private String date_finish;

    @Element
    private int priority;

    @Element
    private int enable_discussion;

    @Element
    private int enable_rewatching;

    @Element
    private String comments;

    @Element
    private String tags;

    public MalAnimeValues() {
        this.episode = episode;
        this.status = status;
        this.score = score;
        this.storage_type = storage_type;
        this.storage_value = storage_value;
        this.times_rewatched = times_rewatched;
        this.rewatch_value = rewatch_value;
        this.priority = priority;
        this.enable_discussion = enable_discussion;
        this.enable_rewatching = enable_rewatching;
        this.comments = "";
        this.tags = "";
        this.date_finish = "";
        this.date_start = "";
    }
}
