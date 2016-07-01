package me.jakemoritz.animebuzz.xml_holders.ANN;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "ann")
public class ANNXMLHolder {

    @ElementList(inline=true)
    private List<AnimeHolder> animeList;

    public List<AnimeHolder> getAnimeList() {
        return animeList;
    }
}
