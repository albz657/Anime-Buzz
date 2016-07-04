package me.jakemoritz.animebuzz.api.mal.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="myanimelist", strict = false)
public class UserListHolder {

    @ElementList(inline=true)
    private List<AnimeListHolder> animeList;

    public List<AnimeListHolder> getAnimeList() {
        return animeList;
    }
}
