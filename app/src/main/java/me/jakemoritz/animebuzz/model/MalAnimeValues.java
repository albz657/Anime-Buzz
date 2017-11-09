package me.jakemoritz.animebuzz.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "entry")
public class MalAnimeValues {

    @Element
    private int episode;

    private int status;

    private int score;

    private int storage_type;

    private float storage_value;

    private int times_rewatched;

    private int rewatch_value;

    private String date_start;

    private String date_finish;

    private int priority;

    private int enable_discussion;

    private int enable_rewatching;

    private String comments;

    private String tags;

}
