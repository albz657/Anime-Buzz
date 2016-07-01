package me.jakemoritz.animebuzz.xml_holders.ANN;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="anime", strict=false)
public class AnimeHolder {

    @Attribute(name="id")
    private String ANNID;

    public String getANNID() {
        return ANNID;
    }

    @ElementList(inline=true)
    private List<InfoHolder> infoList;

    public List<InfoHolder> getInfoList() {
        return infoList;
    }
}
