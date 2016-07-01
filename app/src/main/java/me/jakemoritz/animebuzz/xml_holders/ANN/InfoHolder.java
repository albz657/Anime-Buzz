package me.jakemoritz.animebuzz.xml_holders.ANN;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="info", strict = false)
public class InfoHolder {

    @ElementList(inline=true, required = false)
    private List<ImgHolder> imgList;

    public List<ImgHolder> getImgList() {
        return imgList;
    }
}
