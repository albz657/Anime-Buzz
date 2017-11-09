package me.jakemoritz.animebuzz.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="myanimelist", strict=false)
public class MalUserObject {

    @ElementList(inline=true, required=false, empty=false)
    private List<MalUserAnime> userAnimeList;

    public List<MalUserAnime> getUserAnimeList() {
        return userAnimeList;
    }
}
