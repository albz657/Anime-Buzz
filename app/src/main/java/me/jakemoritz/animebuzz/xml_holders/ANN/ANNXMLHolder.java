package me.jakemoritz.animebuzz.xml_holders.ANN;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ann")
public class ANNXMLHolder {

    @Element(name="anime")
    AnimeHolder anime;

}
