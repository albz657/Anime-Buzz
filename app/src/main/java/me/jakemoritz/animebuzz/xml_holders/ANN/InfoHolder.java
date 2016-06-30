package me.jakemoritz.animebuzz.xml_holders.ANN;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="info", strict = false)
public class InfoHolder {

    @ElementList(inline=true, required = false)
    List<ImgHolder> imgList;

}
