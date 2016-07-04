package me.jakemoritz.animebuzz.api.mal.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="anime", strict=false)
public class AnimeListHolder {

    @Element(name="series_animedb_id")
    private String MALID;

    @Element(name="my_status")
    private String my_status;

    public String getMALID() {
        return MALID;
    }

    public String getMy_status() {
        return my_status;
    }
}
