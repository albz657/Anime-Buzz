package me.jakemoritz.animebuzz.api.ann.models;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

@Root(name="info", strict = false)
@Convert(InfoHolder.InfoConverter.class)
public class InfoHolder {

    private String englishTitle;
    private String imageURL;

    static class InfoConverter implements Converter<InfoHolder> {
        @Override
        public InfoHolder read(InputNode node) throws Exception {
            String value = node.getValue();
            InfoHolder infoHolder = new InfoHolder();

            if (value == null){
                InputNode nextNode = node.getNext();
                while (nextNode != null){
                    String tag = nextNode.getName();

                    if (tag.equals("img") && nextNode.getAttribute("src") != null){
                        infoHolder.imageURL = nextNode.getAttribute("src").getValue();
                    }
                    nextNode= node.getNext();
                }
            } else {
                while (node != null){
                    if (node.getAttribute("lang") != null && node.getAttribute("type") != null){
                        if (node.getAttribute("lang").getValue().equals("EN") && node.getAttribute("type").getValue().equals("Main title")){
                            infoHolder.englishTitle = value;
                        }
                    }
                    node = node.getNext();
                }
            }

            return infoHolder;
        }

        @Override
        public void write(OutputNode node, InfoHolder value) throws Exception {

        }
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public String getImageURL() {
        return imageURL;
    }
}
