package me.jakemoritz.animebuzz.xml_holders.ANN;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="anime", strict=false)
public class AnimeHolder {

    @Attribute(name="id")
    String ANNID;

    @ElementList(inline=true)
    List<InfoHolder> infoList;

}
