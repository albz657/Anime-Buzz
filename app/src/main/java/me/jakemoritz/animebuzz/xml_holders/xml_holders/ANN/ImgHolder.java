package me.jakemoritz.animebuzz.xml_holders.xml_holders.ANN;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="img", strict=false)
public class ImgHolder {

    @Attribute(name="src")
    private String URL;


    public String getURL() {
        return URL;
    }
}
